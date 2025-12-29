package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.enums.ProposalStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalRepository;
import modulemanagement.ls1.shared.PdfCreator;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ModuleVersionService {
    private final ModuleVersionRepository moduleVersionRepository;
    private final ProposalRepository proposalRepository;
    private final OverlapDetectionService overlapDetectionService;
    private final PdfCreator pdfCreator;

    public ModuleVersionService(ModuleVersionRepository moduleVersionRepository, ProposalRepository proposalRepository,
            OverlapDetectionService overlapDetectionService, PdfCreator pdfCreator) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.proposalRepository = proposalRepository;
        this.overlapDetectionService = overlapDetectionService;
        this.pdfCreator = pdfCreator;
    }

    private boolean hasAccessPermission(Proposal proposal, User user) {
        if (proposal.getCreatedBy().getUserId().equals(user.getUserId())) {
            return true;
        }

        return user.getRole() == UserRole.QUALITY_MANAGEMENT ||
                user.getRole() == UserRole.EXAMINATION_BOARD ||
                user.getRole() == UserRole.ACADEMIC_PROGRAM_ADVISOR;
    }

    public ModuleVersionUpdateResponseDTO updateModuleVersionFromRequest(UUID userId, Long moduleVersionId,
            ModuleVersionUpdateRequestDTO request) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleVersion not found"));
        if (!mv.getProposal().getCreatedBy().getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");

        if (!mv.getVersion().equals(mv.getProposal().getLatestModuleVersionWithContent().getVersion())) {
            throw new OptimisticLockingFailureException("Cannot update an outdated ModuleVersion");
        }

        if (!mv.getStatus().equals(ModuleVersionStatus.PENDING_SUBMISSION)) {
            throw new IllegalStateException("Cannot update a submitted ModuleVersion");
        }

        mv.setBulletPoints(request.getBulletPoints());
        mv.setTitleEng(request.getTitleEng());
        mv.setLevelEng(request.getLevelEng());
        mv.setLanguageEng(request.getLanguageEng());
        mv.setFrequencyEng(request.getFrequencyEng());
        mv.setCredits(request.getCredits());
        mv.setDuration(request.getDuration());
        mv.setHoursTotal(request.getHoursTotal());
        mv.setHoursSelfStudy(request.getHoursSelfStudy());
        mv.setHoursPresence(request.getHoursPresence());
        mv.setExaminationAchievementsEng(request.getExaminationAchievementsEng());
        mv.setExaminationAchievementsPromptEng(request.getExaminationAchievementsPromptEng());
        mv.setRepetitionEng(request.getRepetitionEng());
        mv.setRecommendedPrerequisitesEng(request.getRecommendedPrerequisitesEng());
        mv.setContentEng(request.getContentEng());
        mv.setContentPromptEng(request.getContentPromptEng());
        mv.setLearningOutcomesEng(request.getLearningOutcomesEng());
        mv.setLearningOutcomesPromptEng(request.getLearningOutcomesPromptEng());
        mv.setTeachingMethodsEng(request.getTeachingMethodsEng());
        mv.setTeachingMethodsPromptEng(request.getTeachingMethodsPromptEng());
        mv.setMediaEng(request.getMediaEng());
        mv.setLiteratureEng(request.getLiteratureEng());
        mv.setResponsiblesEng(request.getResponsiblesEng());
        mv.setLvSwsLecturerEng(request.getLvSwsLecturerEng());

        mv = moduleVersionRepository.save(mv);
        return ModuleVersionUpdateResponseDTO.fromModuleVersion(mv);
    }

    public void updateStatus(Long moduleVersionId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(
                () -> new ResourceNotFoundException("Could not update corresponding module version status"));
        if (mv.getStatus().equals(ModuleVersionStatus.CANCELLED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal was cancelled by the submitter.");
        }
        Proposal p = mv.getProposal();
        if (!mv.equals(p.getLatestModuleVersionWithContent())) {
            return;
        }

        boolean allFeedbackPositive = true;
        boolean oneFeedbackNegative = false;
        boolean oneFeedbackRejected = false;
        for (Feedback feedback : mv.getRequiredFeedbacks()) {
            if (!feedback.getStatus().equals(FeedbackStatus.APPROVED)) {
                allFeedbackPositive = false;
            }
            if (feedback.getStatus().equals(FeedbackStatus.FEEDBACK_GIVEN)) {
                oneFeedbackNegative = true;
            }
            if (feedback.getStatus().equals(FeedbackStatus.REJECTED)) {
                oneFeedbackRejected = true;
            }
        }

        if (allFeedbackPositive) {
            mv.setStatus(ModuleVersionStatus.ACCEPTED);
            p.setStatus(ProposalStatus.ACCEPTED);
            mv.getProposal().setStatus(ProposalStatus.ACCEPTED);
        }
        if (oneFeedbackNegative) {
            mv.setStatus(ModuleVersionStatus.FEEDBACK_GIVEN);
            p.setStatus(ProposalStatus.REQUIRES_REVIEW);
        }
        if (oneFeedbackRejected) {
            mv.setStatus(ModuleVersionStatus.REJECTED);
            p.setStatus(ProposalStatus.REJECTED);
        }
        proposalRepository.save(p);
        moduleVersionRepository.save(mv);
    }

    public ModuleVersionUpdateResponseDTO getModuleVersionUpdateDtoFromId(Long moduleVersionId, UUID userId) {
        var mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Module Version not found"));
        Proposal p = mv.getProposal();
        if (!p.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        if (p.getModuleVersions() == null || p.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        return ModuleVersionUpdateResponseDTO.fromModuleVersion(mv);
    }

    public ModuleVersionViewDTO getModuleVersionViewDto(Long moduleVersionId, UUID userId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find a module version with this ID."));
        Proposal p = mv.getProposal();
        if (!p.getCreatedBy().getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access.");
        return ModuleVersionViewDTO.from(mv);
    }

    public List<SimilarModuleDTO> getSimilarModules(Long moduleVersionId, User user) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find a module version with this ID."));

        if (!hasAccessPermission(mv.getProposal(), user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        return this.overlapDetectionService.checkModuleOverlap(mv);
    }

    public List<ModuleVersionViewFeedbackDTO> getPreviousModuleVersionFeedback(UUID userId, Long moduleVersionId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find a module version with this ID."));
        Proposal proposal = mv.getProposal();
        if (!proposal.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        return proposal.getPreviousModuleVersionFeedback();
    }

    public Resource generateReviewerModuleVersionPdf(Long moduleVersionId, User user) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Module Version not found"));

        return pdfCreator.createModuleVersionPdf(mv);
    }

    public Resource generateProfessorModuleVersionPdf(Long moduleVersionId, UUID userId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find a module version with this ID."));
        Proposal proposal = mv.getProposal();
        if (!proposal.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        return pdfCreator.createProfessorModuleVersionPdf(mv);
    }
}
