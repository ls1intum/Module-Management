package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.ModuleDegreeProgramAssignmentDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.enums.ProposalStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.DegreeProgram;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.ModuleVersionDegreeProgramAssignment;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.DegreeProgramRepository;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalRepository;
import modulemanagement.ls1.shared.PdfCreator;
import modulemanagement.ls1.shared.ProposalWorkflowStatusDeriver;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import modulemanagement.ls1.shared.ModuleVersionStepsChangeDetector;
import org.springframework.core.io.Resource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ModuleVersionService {
    private final ModuleVersionRepository moduleVersionRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final ProposalRepository proposalRepository;
    private final OverlapDetectionService overlapDetectionService;
    private final PdfCreator pdfCreator;
    private final FeedbackRepository feedbackRepository;
    private final EntityManager entityManager;

    public ModuleVersionService(ModuleVersionRepository moduleVersionRepository,
            DegreeProgramRepository degreeProgramRepository, ProposalRepository proposalRepository,
            OverlapDetectionService overlapDetectionService, PdfCreator pdfCreator,
            FeedbackRepository feedbackRepository, EntityManager entityManager) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.degreeProgramRepository = degreeProgramRepository;
        this.proposalRepository = proposalRepository;
        this.overlapDetectionService = overlapDetectionService;
        this.feedbackRepository = feedbackRepository;
        this.pdfCreator = pdfCreator;
        this.entityManager = entityManager;
    }

    @Transactional
    public ModuleVersionViewDTO updateModuleVersionFromRequest(UUID userId, Long moduleVersionId,
            ModuleVersionUpdateRequestDTO request) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleVersion not found"));
        if (!mv.getProposal().getCreatedBy().getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");

        if (!mv.getVersion().equals(mv.getProposal().getLatestModuleVersionWithContent().getVersion())) {
            throw new OptimisticLockingFailureException("Cannot update an outdated ModuleVersion");
        }
        if (!mv.getRequiredFeedbacks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot update ModuleVersion with feedback.");
        }

        boolean step1Changed = ModuleVersionStepsChangeDetector.isStep1DataChanged(request, mv);
        boolean postStep1Changed = ModuleVersionStepsChangeDetector.isPostStep1DataChanged(request, mv);

        applyUpdateRequest(mv, request);

        if (step1Changed) {
            invalidateActiveFeedbacksAndResetStatuses(mv);
        } else if (postStep1Changed) {
            invalidateExaminationBoardFeedbacksAndRewindExamPhase(mv);
        }

        mv = moduleVersionRepository.save(mv);
        return ModuleVersionViewDTO.from(mv);
    }

    private void invalidateActiveFeedbacksAndResetStatuses(ModuleVersion mv) {
        Proposal proposal = mv.getProposal();

        List<Feedback> activeFeedbacks = feedbackRepository
                .findByModuleVersion_Proposal_ProposalIdAndInvalidatedFalse(proposal.getProposalId());
        if (activeFeedbacks == null || activeFeedbacks.isEmpty()) {
            return;
        }

        for (Feedback f : activeFeedbacks) {
            f.setInvalidated(true);
        }
        feedbackRepository.saveAll(activeFeedbacks);

        proposal.setStatus(ProposalStatus.WAITING_FOR_COORDINATORS_SUBMISSION);
        mv.setStatus(ModuleVersionStatus.WAITING_FOR_COORDINATORS_SUBMISSION);
        proposalRepository.save(proposal);
    }

    /**
     * When curriculum/content (steps after step 1) changes, prior examination board member
     * feedback is no longer valid. Invalidate only those rows and return the workflow to
     * examination-board submission; coordinator feedback is left unchanged.
     */
    private void invalidateExaminationBoardFeedbacksAndRewindExamPhase(ModuleVersion latestDraft) {
        Proposal proposal = latestDraft.getProposal();
        List<Feedback> activeFeedbacks = feedbackRepository
                .findByModuleVersion_Proposal_ProposalIdAndInvalidatedFalse(proposal.getProposalId());
        if (activeFeedbacks == null || activeFeedbacks.isEmpty()) {
            return;
        }
        List<Feedback> examinationBoardMemberFeedbacks = ProposalWorkflowStatusDeriver
                .examinationBoardMemberFeedbacksToInvalidate(activeFeedbacks);
        if (examinationBoardMemberFeedbacks.isEmpty()) {
            return;
        }
        for (Feedback f : examinationBoardMemberFeedbacks) {
            f.setInvalidated(true);
        }
        feedbackRepository.saveAll(examinationBoardMemberFeedbacks);
        proposal.setStatus(ProposalStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION);
        latestDraft.setStatus(ModuleVersionStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION);
        proposalRepository.save(proposal);
    }

    /**
     * Applies request content and degree program assignments to the given module
     * version.
     */
    private void applyUpdateRequest(ModuleVersion mv, ModuleVersionUpdateRequestDTO request) {
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

        if (request.getDegreeProgramAssignments() != null) {
            List<ModuleDegreeProgramAssignmentDTO> assignments = request.getDegreeProgramAssignments();
            if (!assignments.isEmpty()) {
                List<Long> programIds = assignments.stream()
                        .map(ModuleDegreeProgramAssignmentDTO::getDegreeProgramId)
                        .collect(Collectors.toList());
                if (programIds.size() != programIds.stream().distinct().count()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A module cannot be assigned to the same degree program more than once.");
                }
            }

            mv.getDegreeProgramAssignments().clear();
            entityManager.flush();
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
    }

    public void updateStatus(Long moduleVersionId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(
                () -> new ResourceNotFoundException("Could not update corresponding module version status"));
        if (mv.getStatus().equals(ModuleVersionStatus.CANCELLED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal was cancelled by the submitter.");
        }
        Proposal p = mv.getProposal();

        List<Feedback> allFeedbacks = mv.getRequiredFeedbacks() != null ? mv.getRequiredFeedbacks() : new ArrayList<>();
        // Invalidated feedbacks must not affect proposal/module version status.
        var derived = ProposalWorkflowStatusDeriver.derive(allFeedbacks);
        if (derived.isEmpty()) {
            return;
        }
        mv.setStatus(derived.get().moduleVersionStatus());
        p.setStatus(derived.get().proposalStatus());

        syncWorkflowStatusToLatestModuleVersion(p, mv);
        proposalRepository.save(p);
        moduleVersionRepository.save(mv);
    }

    /**
     * After coordinator feedback, a newer draft module version may exist; keep its
     * workflow status aligned with the version that holds the feedbacks.
     */
    private void syncWorkflowStatusToLatestModuleVersion(Proposal p, ModuleVersion mvWithFeedbacks) {
        ModuleVersion latest = p.getLatestModuleVersionWithContent();
        if (latest == null || latest.getModuleVersionId().equals(mvWithFeedbacks.getModuleVersionId())) {
            return;
        }
        latest.setStatus(mvWithFeedbacks.getStatus());
        moduleVersionRepository.save(latest);
    }

    public ModuleVersionViewDTO getModuleVersion(Long moduleVersionId, UUID userId) {
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
        List<Feedback> list = feedbackRepository
                .findByModuleVersion_Proposal_ProposalIdAndInvalidatedFalse(proposal.getProposalId());
        return list.stream().map(ModuleVersionViewFeedbackDTO::from).toList();
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

    ///
    ///
    private boolean hasAccessPermission(Proposal proposal, User user) {
        if (proposal.getCreatedBy().getUserId().equals(user.getUserId())) {
            return true;
        }

        return user.getRoles() != null && (user.getRoles().contains(UserRole.QUALITY_MANAGEMENT)
                || user.getRoles().contains(UserRole.EXAMINATION_BOARD)
                || user.getRoles().contains(UserRole.ACADEMIC_PROGRAM_ADVISOR));
    }
}
