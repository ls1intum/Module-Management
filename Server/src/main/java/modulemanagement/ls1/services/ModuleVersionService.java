package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.ModuleDegreeProgramAssignmentDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
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
import modulemanagement.ls1.repositories.ModuleVersionDegreeProgramAssignmentRepository;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalRepository;
import modulemanagement.ls1.shared.PdfCreator;
import modulemanagement.ls1.shared.ResourceNotFoundException;
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
    private final ModuleVersionDegreeProgramAssignmentRepository assignmentRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final ProposalRepository proposalRepository;
    private final OverlapDetectionService overlapDetectionService;
    private final PdfCreator pdfCreator;
    private final EntityManager entityManager;
    private final FeedbackRepository feedbackRepository;

    public ModuleVersionService(ModuleVersionRepository moduleVersionRepository,
            ModuleVersionDegreeProgramAssignmentRepository assignmentRepository,
            DegreeProgramRepository degreeProgramRepository, ProposalRepository proposalRepository,
            OverlapDetectionService overlapDetectionService, PdfCreator pdfCreator,
            EntityManager entityManager, FeedbackRepository feedbackRepository) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.assignmentRepository = assignmentRepository;
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

        applyUpdateRequest(mv, request);
        mv = moduleVersionRepository.save(mv);
        return ModuleVersionViewDTO.from(mv);
    }

    /**
     * True if any step-1 field (basic info + assignments) differs between request
     * and mv.
     */
    // private boolean isStep1DataChanged(ModuleVersionUpdateRequestDTO request,
    // ModuleVersion mv) {
    // if (!Objects.equals(nullToEmpty(request.getTitleEng()),
    // nullToEmpty(mv.getTitleEng())))
    // return true;
    // if (!Objects.equals(nullToEmpty(request.getTitleDe()),
    // nullToEmpty(mv.getTitleDe())))
    // return true;
    // if (!Objects.equals(request.getCredits(), mv.getCredits()))
    // return true;
    // if (!Objects.equals(nullToEmpty(request.getFrequencyEng()),
    // nullToEmpty(mv.getFrequencyEng())))
    // return true;
    // if (!Objects.equals(request.getHoursLecture(), mv.getHoursLecture()))
    // return true;
    // if (!Objects.equals(request.getHoursExercise(), mv.getHoursExercise()))
    // return true;
    // if (!Objects.equals(request.getHoursPractical(), mv.getHoursPractical()))
    // return true;
    // if (!Objects.equals(request.getHoursSeminar(), mv.getHoursSeminar()))
    // return true;
    // if (!Objects.equals(nullToEmpty(request.getFirstSemesterAvailable()),
    // nullToEmpty(mv.getFirstSemesterAvailable())))
    // return true;
    // if (!Objects.equals(nullToEmpty(request.getSuccessorModuleName()),
    // nullToEmpty(mv.getSuccessorModuleName())))
    // return true;
    // if (!Objects.equals(request.getLanguageEng(), mv.getLanguageEng()))
    // return true;
    // if (request.getDegreeProgramAssignments() != null
    // && !assignmentSetEquals(request.getDegreeProgramAssignments(),
    // mv.getDegreeProgramAssignments())) {
    // return true;
    // }
    // return false;
    // }

    // private static String nullToEmpty(String s) {
    // return s == null ? "" : s.trim();
    // }

    // private static boolean
    // assignmentSetEquals(List<ModuleDegreeProgramAssignmentDTO> requestList,
    // List<ModuleVersionDegreeProgramAssignment> mvList) {
    // Set<String> requestSet = new HashSet<>();
    // if (requestList != null) {
    // for (ModuleDegreeProgramAssignmentDTO a : requestList) {
    // if (a != null && a.getDegreeProgramId() != null &&
    // a.getDegreeProgramSpecializationId() != null) {
    // requestSet.add(a.getDegreeProgramId() + "," +
    // a.getDegreeProgramSpecializationId());
    // }
    // }
    // }
    // Set<String> mvSet = new HashSet<>();
    // if (mvList != null) {
    // for (ModuleVersionDegreeProgramAssignment a : mvList) {
    // if (a.getDegreeProgram() != null && a.getDegreeProgramSpecialization() !=
    // null) {
    // mvSet.add(a.getDegreeProgram().getDegreeProgramId() + ","
    // + a.getDegreeProgramSpecialization().getDegreeProgramSpecializationId());
    // }
    // }
    // }
    // return requestSet.equals(mvSet);
    // }

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

            assignmentRepository.deleteByModuleVersion_ModuleVersionId(mv.getModuleVersionId());
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
        if (!mv.equals(p.getLatestModuleVersionWithContent())) {
            return;
        }

        List<Feedback> allFeedbacks = mv.getRequiredFeedbacks() != null ? mv.getRequiredFeedbacks() : new ArrayList<>();
        // Coordinator feedbacks for current assignments + all role-based feedbacks (we
        // only have one active set of pending at a time).
        List<Feedback> coordinatorFeedbacks = allFeedbacks.stream()
                .filter(f -> f.getDegreeProgramSpecialization() != null)
                .toList();
        List<Feedback> roleBased = allFeedbacks.stream()
                .filter(f -> f.getRequiredRole() != null)
                .toList();
        List<Feedback> feedbacksToEvaluate = new ArrayList<>(coordinatorFeedbacks);
        feedbacksToEvaluate.addAll(roleBased);

        boolean allFeedbackPositive = true;
        boolean oneFeedbackNegative = false;
        boolean oneFeedbackRejected = false;
        for (Feedback feedback : feedbacksToEvaluate) {
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
            boolean anyRoleBased = !roleBased.isEmpty();
            if (anyRoleBased) {
                mv.setStatus(ModuleVersionStatus.ACCEPTED);
                p.setStatus(ProposalStatus.ACCEPTED);
            } else {
                mv.setStatus(ModuleVersionStatus.PENDING_FULL_SUBMISSION);
                p.setStatus(ProposalStatus.PENDING_FULL_SUBMISSION);
            }
        }
        if (oneFeedbackNegative) {
            mv.setStatus(ModuleVersionStatus.REQUIRES_REVIEW);
            p.setStatus(ProposalStatus.REQUIRES_REVIEW);
        }
        if (oneFeedbackRejected) {
            mv.setStatus(ModuleVersionStatus.REJECTED);
            p.setStatus(ProposalStatus.REJECTED);
        }
        proposalRepository.save(p);
        moduleVersionRepository.save(mv);
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
