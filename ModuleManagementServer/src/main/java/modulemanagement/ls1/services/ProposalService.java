package modulemanagement.ls1.services;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.AddModuleVersionDTO;
import modulemanagement.ls1.dtos.ProposalRequestDTO;
import modulemanagement.ls1.dtos.ProposalViewDTO;
import modulemanagement.ls1.dtos.ProposalsCompactDTO;
import modulemanagement.ls1.enums.*;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalRepository;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ModuleVersionRepository moduleVersionRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public ProposalService(ProposalRepository proposalRepository, ModuleVersionRepository moduleVersionRepository, FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.proposalRepository = proposalRepository;
        this.moduleVersionRepository = moduleVersionRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public Proposal createProposalFromRequest(User user, ProposalRequestDTO request) {
//        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getRole().equals(UserRole.PROFESSOR))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be a professor in order to propose a module.");
        Proposal p = new Proposal();
        p.setCreatedBy(user);
        p.setCreationDate(LocalDateTime.now());
        p.setStatus(ProposalStatus.PENDING_SUBMISSION);
        p = proposalRepository.save(p);

        ModuleVersion mv = new ModuleVersion();
        mv.setVersion(1);
        mv.setModuleId(null);
        mv.setCreationDate(LocalDateTime.now());
        mv.setProposal(p);
        mv.setStatus(ModuleVersionStatus.PENDING_SUBMISSION);
        mv.setTitleEng(request.getTitleEng());
        mv.setLevelEng(request.getLevelEng());
        mv.setLanguageEng(request.getLanguageEng());
        mv.setFrequencyEng(request.getFrequencyEng());
        mv.setCredits(request.getCredits());
        mv.setHoursTotal(request.getHoursTotal());
        mv.setHoursSelfStudy(request.getHoursSelfStudy());
        mv.setHoursPresence(request.getHoursPresence());
        mv.setExaminationAchievementsEng(request.getExaminationAchievementsEng());
        mv.setRepetitionEng(request.getRepetitionEng());
        mv.setRecommendedPrerequisitesEng(request.getRecommendedPrerequisitesEng());
        mv.setContentEng(request.getContentEng());
        mv.setLearningOutcomesEng(request.getLearningOutcomesEng());
        mv.setTeachingMethodsEng(request.getTeachingMethodsEng());
        mv.setMediaEng(request.getMediaEng());
        mv.setLiteratureEng(request.getLiteratureEng());
        mv.setResponsiblesEng(request.getResponsiblesEng());
        mv.setLvSwsLecturerEng(request.getLvSwsLecturerEng());
        mv = moduleVersionRepository.save(mv);

        List<Feedback> feedbacks = new ArrayList<>();
        createNewFeedbacks(mv, feedbacks);
        feedbacks = feedbackRepository.saveAll(feedbacks);
        mv.setRequiredFeedbacks(feedbacks);
        moduleVersionRepository.save(mv);
        p.addModuleVersion(mv);
        proposalRepository.save(p);
        return p;
    }

    public static void createNewFeedbacks(ModuleVersion mv, List<Feedback> feedbacks) {
        for (UserRole ad : UserRole.values()) {
            if (ad.equals(UserRole.PROFESSOR))
                continue;
            Feedback feedback = new Feedback();
            feedback.setStatus(FeedbackStatus.PENDING_SUBMISSION);
            feedback.setRequiredRole(ad);
            feedback.setModuleVersion(mv);
            feedbacks.add(feedback);
        }
    }

    public ProposalViewDTO addModuleVersion(@Valid AddModuleVersionDTO request) {
        Proposal p = proposalRepository.findById(request.getProposalId()).orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getUserId().equals(p.getCreatedBy().getUserId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot add a module version to a module you did not create.");
        if (!p.getStatus().equals(ProposalStatus.REQUIRES_REVIEW))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only add a new module version, if the proposal requires a review.");

        p.addNewModuleVersion();
        proposalRepository.save(p);
        return p.toProposalViewDTO();
    }

    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Proposal::getProposalId))
                .collect(Collectors.toList());
    }

    public List<ProposalsCompactDTO> getAllProposalsCompact() {
        return proposalRepository.findAll()
                .stream()
                .map(p -> new ProposalsCompactDTO(
                        p.getProposalId(),
                        p.getCreatedBy().getFirstName(),
                        p.getStatus(),
                        p.getLatestModuleVersionWithContent() != null ? p.getLatestModuleVersionWithContent().getModuleVersionId() : null,
                        p.getLatestModuleVersionWithContent() != null ? p.getLatestModuleVersionWithContent().getTitleEng() : null
                ))
                .sorted(Comparator.comparing(ProposalsCompactDTO::getProposalId))
                .collect(Collectors.toList());
    }

    public List<Proposal> getProposalsOfUser(UUID userId) {
        return proposalRepository.findAll()
                .stream()
                .filter(proposal -> proposal.getCreatedBy().getUserId().equals(userId))
                .sorted(Comparator.comparing(Proposal::getProposalId))
                .collect(Collectors.toList());
    }

    public List<ProposalsCompactDTO> getCompactProposalsOfUser(UUID userId) {
        return proposalRepository.findAll()
                .stream()
                .filter(proposal -> proposal.getCreatedBy().getUserId().equals(userId))
                .map(p -> new ProposalsCompactDTO(
                        p.getProposalId(),
                        p.getCreatedBy().getFirstName(),
                        p.getStatus(),
                        p.getLatestModuleVersionWithContent() != null ? p.getLatestModuleVersionWithContent().getModuleVersionId() : null,
                        p.getLatestModuleVersionWithContent() != null ? p.getLatestModuleVersionWithContent().getTitleEng() : null
                ))
                .sorted(Comparator.comparing(ProposalsCompactDTO::getProposalId))
                .collect(Collectors.toList());

    }

    public Proposal getProposalById(long id) {
        return proposalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
    }

    public ProposalViewDTO getProposalViewDtoById(long id) {
        var p = proposalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        // TODO: AUTH
        return p.toProposalViewDTO();

    }

    public ProposalViewDTO submitProposal(Long proposalId, UUID userId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("No proposal with id " + proposalId +" found"));

        if (!proposal.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        if (proposal.getModuleVersions() == null || proposal.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        ModuleVersion mv = proposal.getLatestModuleVersionWithContent();

        if (!mv.getStatus().equals(ModuleVersionStatus.PENDING_SUBMISSION)) {
            throw new IllegalStateException("Proposal is not pending submission. It is " + mv.getStatus() + ".");
        }

        if (!mv.isCompleted()) {
            throw new IllegalStateException("All required fields in ModuleVersion must be filled.");
        }

        mv.setStatus(ModuleVersionStatus.PENDING_FEEDBACK);
        proposal.setStatus(ProposalStatus.PENDING_FEEDBACK);
        List<Feedback> feedbacks = mv.getRequiredFeedbacks();
        for (Feedback feedback : feedbacks) {
            feedback.setStatus(FeedbackStatus.PENDING_FEEDBACK);
        }
        feedbackRepository.saveAll(feedbacks);
        moduleVersionRepository.save(mv);
        proposalRepository.save(proposal);
        return proposal.toProposalViewDTO();
    }

    public void deleteProposalById(long proposalId, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        Proposal p = proposalRepository.findById(proposalId).orElseThrow(() -> new ResourceNotFoundException("Proposal with id " + proposalId + " not found."));
        if (!p.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        if (p.getStatus() != ProposalStatus.PENDING_SUBMISSION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only delete a proposal that is not already submit. This module proposal is " + p.getStatus() + ".");
        }
        proposalRepository.delete(p);
    }

}
