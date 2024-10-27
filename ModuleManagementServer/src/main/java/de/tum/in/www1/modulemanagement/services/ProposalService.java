package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.dtos.ProposalRequestDTO;
import de.tum.in.www1.modulemanagement.enums.FeedbackStatus;
import de.tum.in.www1.modulemanagement.models.Feedback;
import de.tum.in.www1.modulemanagement.models.ModuleVersion;
import de.tum.in.www1.modulemanagement.models.Proposal;
import de.tum.in.www1.modulemanagement.models.User;
import de.tum.in.www1.modulemanagement.repositories.FeedbackRepository;
import de.tum.in.www1.modulemanagement.repositories.ModuleVersionRepository;
import de.tum.in.www1.modulemanagement.repositories.ProposalRepository;
import de.tum.in.www1.modulemanagement.repositories.UserRepository;
import de.tum.in.www1.modulemanagement.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    public Proposal createProposalFromRequest(ProposalRequestDTO request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Proposal p = new Proposal();
        p.setCreatedBy(user);
        p.setCreationDate(LocalDateTime.now());
        p = proposalRepository.save(p);

        ModuleVersion mv = new ModuleVersion();
        mv.setVersion(1);
        mv.setModuleId(null);
        mv.setProposal(p);
        mv.setStatus(request.isSubmitImmediately() ? "PENDING_FEEDBACK" : "PENDING_SUBMISSION");
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
        mv.setRecommendedPrerequisitesEng(request.getRecommendedPrerequisiteEng());
        mv.setContentEng(request.getContentEng());
        mv.setLearningOutcomesEng(request.getLearningOutcomesEng());
        mv.setTeachingMethodsEng(request.getTeachingMethodsEng());
        mv.setMediaEng(request.getMediaEng());
        mv.setLiteratureEng(request.getLiteratureEng());
        mv.setResponsiblesEng(request.getResponsiblesEng());
        mv.setLvSwsLecturerEng(request.getLvSwsLecturerEng());
        mv = moduleVersionRepository.save(mv);

        List<Feedback> feedbacks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Feedback feedback = new Feedback();
            feedback.setStatus(FeedbackStatus.PENDING_SUBMISSION);
            feedback.setModuleVersion(mv);
            feedbacks.add(feedback);
        }
        feedbacks = feedbackRepository.saveAll(feedbacks);
        mv.setRequiredFeedbacks(feedbacks);
        moduleVersionRepository.save(mv);
        p.addModuleVersion(mv);
        proposalRepository.save(p);
        return p;
    }

    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Proposal::getProposalId))
                .collect(Collectors.toList());
    }

    public Proposal getProposalById(long id) {
        return proposalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
    }

    public void submitProposal(Long proposalId, Long userId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("No proposal with id " + proposalId +" found"));

        if (!proposal.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        if (proposal.getModuleVersions() == null || proposal.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        ModuleVersion mv = proposal.getLatestModuleVersion();

        if (!mv.getStatus().equals("PENDING_SUBMISSION")) {
            throw new IllegalStateException("Proposal is not pending submission. It is " + mv.getStatus() + ".");
        }

        if (!mv.isCompleted()) {
            throw new IllegalStateException("All required fields in ModuleVersion must be filled.");
        }

        mv.setStatus("PENDING_FEEDBACK");
        proposalRepository.save(proposal);
    }
}
