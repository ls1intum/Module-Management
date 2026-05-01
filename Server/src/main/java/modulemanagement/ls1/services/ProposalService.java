package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.enums.*;
import modulemanagement.ls1.models.DegreeProgram;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.ExaminationBoard;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.ModuleVersionDegreeProgramAssignment;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.DegreeProgramRepository;
import modulemanagement.ls1.repositories.ExaminationBoardRepository;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Validated
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ModuleVersionRepository moduleVersionRepository;
    private final FeedbackRepository feedbackRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final ExaminationBoardRepository examinationBoardRepository;
    private final MailingService mailingService;

    public ProposalService(ProposalRepository proposalRepository, ModuleVersionRepository moduleVersionRepository,
            FeedbackRepository feedbackRepository, DegreeProgramRepository degreeProgramRepository,
            ExaminationBoardRepository examinationBoardRepository,
            MailingService mailingService) {
        this.proposalRepository = proposalRepository;
        this.moduleVersionRepository = moduleVersionRepository;
        this.feedbackRepository = feedbackRepository;
        this.degreeProgramRepository = degreeProgramRepository;
        this.examinationBoardRepository = examinationBoardRepository;
        this.mailingService = mailingService;
    }

    @Transactional
    public ProposalViewDTO createProposalFromRequest(User user, ProposalRequestDTO request) {
        Proposal p = new Proposal();
        p.setCreatedBy(user);
        p.setCreationDate(LocalDateTime.now());
        p.setStatus(ProposalStatus.WAITING_FOR_COORDINATORS_SUBMISSION);
        p = proposalRepository.save(p);

        ModuleVersion mv = new ModuleVersion();
        mv.setVersion(1);
        mv.setModuleId(null);
        mv.setCreationDate(LocalDateTime.now());
        mv.setProposal(p);
        mv.setStatus(ModuleVersionStatus.WAITING_FOR_COORDINATORS_SUBMISSION);
        mv.setBulletPoints(request.getBulletPoints());
        mv.setTitleEng(request.getTitleEng());
        mv.setTitleDe(request.getTitleDe());
        mv.setLevelEng(request.getLevelEng());
        mv.setLanguageEng(request.getLanguageEng());
        mv.setFrequencyEng(request.getFrequencyEng());
        mv.setCredits(request.getCredits());
        mv.setHoursLecture(request.getHoursLecture());
        mv.setHoursExercise(request.getHoursExercise());
        mv.setHoursPractical(request.getHoursPractical());
        mv.setHoursSeminar(request.getHoursSeminar());
        mv.setFirstSemesterAvailable(request.getFirstSemesterAvailable());
        mv.setSuccessorModuleName(request.getSuccessorModuleName());
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

        if (request.getDegreeProgramAssignments() != null && !request.getDegreeProgramAssignments().isEmpty()) {
            List<Long> programIds = request.getDegreeProgramAssignments().stream()
                    .map(ModuleDegreeProgramAssignmentDTO::getDegreeProgramId)
                    .collect(Collectors.toList());
            if (programIds.size() != programIds.stream().distinct().count()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A module cannot be assigned to the same degree program more than once.");
            }
            for (ModuleDegreeProgramAssignmentDTO item : request.getDegreeProgramAssignments()) {
                DegreeProgram program = degreeProgramRepository
                        .findWithSpecializationsByDegreeProgramId(item.getDegreeProgramId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Degree program not found: " + item.getDegreeProgramId()));
                DegreeProgramSpecialization spec = program.getDegreeProgramSpecializations().stream()
                        .filter(s -> s.getDegreeProgramSpecializationId()
                                .equals(item.getDegreeProgramSpecializationId()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Specialization " + item.getDegreeProgramSpecializationId()
                                        + " does not belong to degree program " + item.getDegreeProgramId()));
                ModuleVersionDegreeProgramAssignment assignment = new ModuleVersionDegreeProgramAssignment();
                assignment.setModuleVersion(mv);
                assignment.setDegreeProgram(program);
                assignment.setDegreeProgramSpecialization(spec);
                mv.getDegreeProgramAssignments().add(assignment);
            }
        }

        mv.setRequiredFeedbacks(new ArrayList<>());
        moduleVersionRepository.save(mv);
        p.getModuleVersions().add(mv);
        proposalRepository.save(p);
        return ProposalViewDTO.from(p);
    }

    public List<ProposalsCompactDTO> getCompactProposalsOfUser(UUID userId) {
        return proposalRepository.findByCreatedBy_UserId(userId).stream()
                .map(p -> new ProposalsCompactDTO(
                        p.getProposalId(),
                        p.getCreatedBy().getFirstName(),
                        p.getStatus(),
                        p.getLatestModuleVersionWithContent() != null
                                ? p.getLatestModuleVersionWithContent().getModuleVersionId()
                                : null,
                        p.getLatestModuleVersionWithContent() != null
                                ? p.getLatestModuleVersionWithContent().getTitleEng()
                                : null))
                .sorted(Comparator.comparing(ProposalsCompactDTO::getProposalId))
                .collect(Collectors.toList());

    }

    public ProposalViewDTO getProposalViewDtoById(UUID userId, long id) {
        var p = proposalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));
        if (!p.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        return ProposalViewDTO.from(p);

    }

    @Transactional
    public ProposalViewDTO requestCoordinatorsFeedback(Long proposalId, UUID userId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("No proposal with id " + proposalId + " found"));
        if (!proposal.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        if (proposal.getModuleVersions() == null || proposal.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        ModuleVersion mv = proposal.getLatestModuleVersionWithContent();

        if (!mv.getStatus().equals(ModuleVersionStatus.WAITING_FOR_COORDINATORS_SUBMISSION)) {
            throw new IllegalStateException(
                    "Proposal is not waiting for coordinator submission. It is " + mv.getStatus() + ".");
        }

        if (!mv.isFirstStepComplete()) {
            throw new IllegalStateException(
                    "Step 1 must be complete: English title and at least one degree program assignment with a chosen specialization.");
        }

        List<Feedback> requiredFeedbacks = new ArrayList<>();

        for (ModuleVersionDegreeProgramAssignment assignment : mv.getDegreeProgramAssignments()) {
            var spec = assignment.getDegreeProgramSpecialization();
            if (spec.getResponsibleUser() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Specialization \"" + spec.getName() + "\" has no responsible user. "
                                + "Assign a program or area coordinator before submitting for feedback.");
            }
            Feedback feedback = new Feedback();
            feedback.setStatus(FeedbackStatus.PENDING_FEEDBACK);
            feedback.setCreatedAt(LocalDateTime.now());
            feedback.setDegreeProgramSpecialization(spec);
            feedback.setAssignedReviewer(spec.getResponsibleUser());
            feedback.setRequiredRole(null);
            feedback.setModuleVersion(mv);
            requiredFeedbacks.add(feedbackRepository.save(feedback));
        }

        mv.setStatus(ModuleVersionStatus.PENDING_COORDINATORS_FEEDBACK);
        proposal.setStatus(ProposalStatus.PENDING_COORDINATORS_FEEDBACK);
        moduleVersionRepository.save(mv);

        // to keep the version with requested feedback immutable
        proposal.addNewModuleVersion();
        proposalRepository.save(proposal);
        mailingService.sendReviewerRequestNotification(
                requiredFeedbacks,
                mv.getTitleEng() != null ? mv.getTitleEng() : "Untitled module");

        return ProposalViewDTO.from(proposal);
    }

    /**
     * Request feedback from examination board members. Only after all content steps
     * are complete,
     * proposal is at examination-board submission, and every coordinator feedback
     * for current
     * assignments is approved.
     */
    @Transactional
    public ProposalViewDTO requestExaminationBoardFeedback(Long proposalId, UUID userId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("No proposal with id " + proposalId + " found"));
        if (!proposal.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        if (proposal.getModuleVersions() == null || proposal.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        ModuleVersion mv = proposal.getLatestModuleVersionWithContent();
        if (!mv.getStatus().equals(ModuleVersionStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION)) {
            throw new IllegalStateException(
                    "Proposal is not waiting for examination board submission. It is " + mv.getStatus() + ".");
        }
        if (!proposal.getStatus().equals(ProposalStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION)) {
            throw new IllegalStateException(
                    "Proposal workflow is not at examination board submission. It is " + proposal.getStatus() + ".");
        }
        if (!mv.isCompleted()) {
            throw new IllegalStateException(
                    "All steps must be completed before submitting for examination board feedback.");
        }

        List<Feedback> coordinatorFeedbacks = currentCoordinatorFeedbacks(proposal, mv);
        if (coordinatorFeedbacks.isEmpty()) {
            throw new IllegalStateException(
                    "No coordinator feedbacks for current assignments. Submit for coordinator feedback first.");
        }
        boolean allCoordinatorAccepted = coordinatorFeedbacks.stream()
                .allMatch(f -> f.getStatus() == FeedbackStatus.APPROVED);
        if (!allCoordinatorAccepted) {
            throw new IllegalStateException(
                    "All coordinator feedbacks must be approved before submitting for examination board feedback.");
        }

        LinkedHashSet<Long> boardIds = new LinkedHashSet<>();
        for (ModuleVersionDegreeProgramAssignment assignment : mv.getDegreeProgramAssignments()) {
            DegreeProgram program = degreeProgramRepository
                    .findById(assignment.getDegreeProgram().getDegreeProgramId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Degree program not found: " + assignment.getDegreeProgram().getDegreeProgramId()));
            if (program.getExaminationBoard() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Degree program \"" + program.getName() + "\" has no examination board assigned. "
                                + "An administrator must link an examination board before you can continue.");
            }
            boardIds.add(program.getExaminationBoard().getExaminationBoardId());
        }

        List<Feedback> newExaminationBoardFeedbacks = new ArrayList<>();
        Set<String> seenMemberPerBoard = new java.util.HashSet<>();
        for (Long boardId : boardIds) {
            ExaminationBoard board = examinationBoardRepository.findByIdWithMembers(boardId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Examination board not found: " + boardId));
            if (board.getMembers() == null || board.getMembers().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Examination board \"" + board.getName() + "\" has no members. Add members in administration.");
            }
            for (User member : board.getMembers()) {
                String dedupeKey = boardId + ":" + member.getUserId();
                if (!seenMemberPerBoard.add(dedupeKey)) {
                    continue;
                }
                Feedback feedback = new Feedback();
                feedback.setStatus(FeedbackStatus.PENDING_FEEDBACK);
                feedback.setCreatedAt(LocalDateTime.now());
                feedback.setRequiredRole(null);
                feedback.setAssignedReviewer(member);
                feedback.setExaminationBoard(board);
                feedback.setDegreeProgramSpecialization(null);
                feedback.setModuleVersion(mv);
                newExaminationBoardFeedbacks.add(feedbackRepository.save(feedback));
            }
        }

        mv.setStatus(ModuleVersionStatus.PENDING_EXAMINATION_BOARD_FEEDBACK);
        proposal.setStatus(ProposalStatus.PENDING_EXAMINATION_BOARD_FEEDBACK);
        moduleVersionRepository.save(mv);

        proposal.addNewModuleVersion();
        proposalRepository.save(proposal);
        mailingService.sendReviewerRequestNotification(
                newExaminationBoardFeedbacks,
                mv.getTitleEng() != null ? mv.getTitleEng() : "Untitled module");

        return ProposalViewDTO.from(proposal);
    }

    public void deleteProposalById(long proposalId, UUID userId) {
        Proposal p = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal with id " + proposalId + " not found."));
        if (!p.getCreatedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        if (!p.getStatus().equals(ProposalStatus.WAITING_FOR_COORDINATORS_SUBMISSION)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You can only delete a proposal that is not already submitted. This module proposal is "
                            + p.getStatus() + ".");
        }
        proposalRepository.delete(p);
    }

    /**
     * Feedback rows are stored on the module version that was current when the
     * request was made;
     * after {@code addNewModuleVersion()}, the latest version may have an empty
     * list while prior
     * versions still hold active coordinator / examination-board rows. Aggregate
     * for validation.
     */
    private static List<Feedback> nonInvalidatedFeedbacksForProposal(Proposal proposal) {
        if (proposal.getModuleVersions() == null || proposal.getModuleVersions().isEmpty()) {
            return List.of();
        }
        return proposal.getModuleVersions().stream()
                .flatMap(m -> m.getRequiredFeedbacks() != null
                        ? m.getRequiredFeedbacks().stream()
                        : Stream.empty())
                .filter(f -> f != null && !f.isInvalidated())
                .toList();
    }

    private static Set<Long> currentAssignmentSpecializationIds(ModuleVersion latest) {
        if (latest.getDegreeProgramAssignments() == null) {
            return Set.of();
        }
        return latest.getDegreeProgramAssignments().stream()
                .map(a -> a.getDegreeProgramSpecialization() != null
                        ? a.getDegreeProgramSpecialization().getDegreeProgramSpecializationId()
                        : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Coordinator slice for specializations currently assigned on {@code latest},
     * any version.
     */
    private static List<Feedback> currentCoordinatorFeedbacks(Proposal proposal, ModuleVersion latest) {
        Set<Long> specIds = currentAssignmentSpecializationIds(latest);
        if (specIds.isEmpty()) {
            return List.of();
        }
        return nonInvalidatedFeedbacksForProposal(proposal).stream()
                .filter(f -> f.getDegreeProgramSpecialization() != null
                        && specIds.contains(f.getDegreeProgramSpecialization().getDegreeProgramSpecializationId())
                        && f.getAssignedReviewer() != null
                        && f.getRequiredRole() == null
                        && f.getExaminationBoard() == null)
                .toList();
    }
}
