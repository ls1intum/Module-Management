package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.ProposalAiReviewDTO;
import modulemanagement.ls1.dtos.ProposalAiReviewSectionDTO;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.AiReviewGuideline;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.ProposalAiReview;
import modulemanagement.ls1.models.ProposalAiReviewSection;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.AiReviewGuidelineRepository;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalAiReviewRepository;
import modulemanagement.ls1.shared.AiReviewGuidelinePromptUtil;
import modulemanagement.ls1.shared.ProposalAiReviewParser;
import modulemanagement.ls1.shared.ProposalAiReviewPromptUtil;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProposalAiReviewService {

    private final ModuleVersionRepository moduleVersionRepository;
    private final AiReviewGuidelineRepository aiReviewGuidelineRepository;
    private final FeedbackRepository feedbackRepository;
    private final ProposalAiReviewRepository proposalAiReviewRepository;
    private final LLMGenerationService llmGenerationService;

    public ProposalAiReviewService(ModuleVersionRepository moduleVersionRepository,
            AiReviewGuidelineRepository aiReviewGuidelineRepository,
            FeedbackRepository feedbackRepository,
            ProposalAiReviewRepository proposalAiReviewRepository,
            LLMGenerationService llmGenerationService) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.aiReviewGuidelineRepository = aiReviewGuidelineRepository;
        this.feedbackRepository = feedbackRepository;
        this.proposalAiReviewRepository = proposalAiReviewRepository;
        this.llmGenerationService = llmGenerationService;
    }

    /**
     * Returns the stored AI review, or generates one if none exists yet.
     * Pass {@code regenerate=true} to force a fresh LLM run and replace the stored result.
     */
    @Transactional
    public ProposalAiReviewDTO getOrGenerateReview(Long moduleVersionId, User user, boolean regenerate) {
        if (!regenerate) {
            requireAccessibleModuleVersion(moduleVersionId, user);
            Optional<ProposalAiReviewDTO> stored = proposalAiReviewRepository
                    .findByModuleVersion_ModuleVersionId(moduleVersionId)
                    .map(this::toDto);
            if (stored.isPresent()) {
                return stored.get();
            }
        }
        return generateReview(moduleVersionId, user);
    }

    /** Generates a fresh review, replacing any stored one for this module version. */
    @Transactional
    public ProposalAiReviewDTO generateReview(Long moduleVersionId, User user) {
        ModuleVersion moduleVersion = requireAccessibleModuleVersion(moduleVersionId, user);
        List<AiReviewGuideline> guidelines = aiReviewGuidelineRepository
                .findAllByOrderBySectionAscSortOrderAscGuidelineIdAsc();

        String basePrompt = ProposalAiReviewPromptUtil.buildReviewPrompt(moduleVersion, guidelines);
        String prompt = ProposalAiReviewPromptUtil.appendSectionGuidelines(basePrompt, guidelines);
        String llmResponse = llmGenerationService.generate(prompt, "proposal-ai-review");

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(llmResponse);
        dto.setModuleVersionId(moduleVersionId);
        dto.setGeneratedAt(LocalDateTime.now());
        dto.setGuidelinesConfigured(!guidelines.isEmpty());

        persistReview(moduleVersion, user, dto);
        return dto;
    }

    private void persistReview(ModuleVersion moduleVersion, User user, ProposalAiReviewDTO dto) {
        ProposalAiReview review = proposalAiReviewRepository
                .findByModuleVersion_ModuleVersionId(moduleVersion.getModuleVersionId())
                .orElseGet(ProposalAiReview::new);
        review.setModuleVersion(moduleVersion);
        review.setSummary(dto.getSummary());
        review.setGuidelinesConfigured(dto.isGuidelinesConfigured());
        review.setGeneratedBy(user);
        review.setGeneratedAt(dto.getGeneratedAt());

        review.getSections().clear();
        int order = 0;
        for (ProposalAiReviewSectionDTO sectionDto : dto.getSections()) {
            ProposalAiReviewSection section = new ProposalAiReviewSection();
            section.setReview(review);
            section.setSection(sectionDto.getSection());
            section.setSeverity(sectionDto.getSeverity());
            section.setFindings(sectionDto.getFindings());
            section.setSuggestions(sectionDto.getSuggestions());
            section.setSortOrder(order++);
            review.getSections().add(section);
        }
        proposalAiReviewRepository.save(review);
    }

    private ProposalAiReviewDTO toDto(ProposalAiReview review) {
        ProposalAiReviewDTO dto = new ProposalAiReviewDTO();
        dto.setModuleVersionId(review.getModuleVersion().getModuleVersionId());
        dto.setSummary(review.getSummary());
        dto.setGeneratedAt(review.getGeneratedAt());
        dto.setGuidelinesConfigured(review.isGuidelinesConfigured());
        List<ProposalAiReviewSectionDTO> sections = new ArrayList<>();
        for (ProposalAiReviewSection section : review.getSections()) {
            ProposalAiReviewSectionDTO sectionDto = new ProposalAiReviewSectionDTO();
            sectionDto.setSection(section.getSection());
            sectionDto.setSectionLabel(AiReviewGuidelinePromptUtil.getSectionLabel(section.getSection()));
            sectionDto.setSeverity(section.getSeverity());
            sectionDto.setFindings(section.getFindings());
            sectionDto.setSuggestions(section.getSuggestions());
            sections.add(sectionDto);
        }
        dto.setSections(sections);
        return dto;
    }

    private ModuleVersion requireAccessibleModuleVersion(Long moduleVersionId, User user) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find a module version with this ID."));
        if (!canAccessModuleVersionForAiReview(moduleVersion, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to review this proposal.");
        }
        return moduleVersion;
    }

    private boolean canAccessModuleVersionForAiReview(ModuleVersion moduleVersion, User user) {
        Proposal proposal = moduleVersion.getProposal();
        if (proposal == null || proposal.getCreatedBy() == null) {
            return false;
        }
        if (proposal.getCreatedBy().getUserId().equals(user.getUserId())) {
            return true;
        }
        if (user.getRoles() == null) {
            return false;
        }
        if (user.getRoles().contains(UserRole.QUALITY_MANAGEMENT)
                || user.getRoles().contains(UserRole.EXAMINATION_BOARD)
                || user.getRoles().contains(UserRole.ACADEMIC_PROGRAM_ADVISOR)) {
            return true;
        }
        if (user.getRoles().contains(UserRole.PROGRAM_COORDINATOR)
                || user.getRoles().contains(UserRole.SPECIALIZATION_AREA_COORDINATOR)) {
            return feedbackRepository.existsByModuleVersion_Proposal_ProposalIdAndInvalidatedFalseAndAssignedReviewer_UserId(
                    proposal.getProposalId(), user.getUserId());
        }
        return false;
    }
}
