package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.enums.ProposalStatus;
import modulemanagement.ls1.models.Feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic derivation of proposal and module-version workflow status from
 * non-invalidated coordinator and examination-board feedback rows.
 */
public final class ProposalWorkflowStatusDeriver {

    private ProposalWorkflowStatusDeriver() {
    }

    public record DerivedWorkflowStatus(ModuleVersionStatus moduleVersionStatus, ProposalStatus proposalStatus) {
    }

    public static boolean isCoordinatorFeedback(Feedback f) {
        return f != null
                && f.getDegreeProgramSpecialization() != null
                && f.getAssignedReviewer() != null
                && f.getRequiredRole() == null
                && f.getExaminationBoard() == null;
    }

    public static boolean isExaminationBoardMemberFeedback(Feedback f) {
        return f != null
                && f.getExaminationBoard() != null
                && f.getAssignedReviewer() != null
                && f.getRequiredRole() == null
                && f.getDegreeProgramSpecialization() == null;
    }

    /**
     * Derives status from the required-feedback list on one module version.
     * Invalidated rows are ignored. Returns empty when no coordinator or
     * examination-board member feedback remains after filtering.
     */
    public static Optional<DerivedWorkflowStatus> derive(List<Feedback> requiredFeedbacks) {
        List<Feedback> all = requiredFeedbacks != null ? requiredFeedbacks : List.of();
        List<Feedback> nonInvalidated = all.stream()
                .filter(f -> f != null && !f.isInvalidated())
                .toList();
        List<Feedback> coordinatorFeedbacks = nonInvalidated.stream()
                .filter(ProposalWorkflowStatusDeriver::isCoordinatorFeedback)
                .toList();
        List<Feedback> examBoardAssigned = nonInvalidated.stream()
                .filter(ProposalWorkflowStatusDeriver::isExaminationBoardMemberFeedback)
                .toList();
        if (coordinatorFeedbacks.isEmpty() && examBoardAssigned.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(applyCoordinatorAndExaminationBoardStatus(coordinatorFeedbacks, examBoardAssigned));
    }

    /**
     * When both slices are non-empty, coordinator outcomes take precedence until every
     * coordinator feedback is {@link FeedbackStatus#APPROVED}, then examination-board rules apply.
     */
    static DerivedWorkflowStatus applyCoordinatorAndExaminationBoardStatus(
            List<Feedback> coordinatorFeedbacks, List<Feedback> examBoardAssigned) {
        boolean hasCoordinator = !coordinatorFeedbacks.isEmpty();
        boolean hasExam = !examBoardAssigned.isEmpty();

        if (hasCoordinator && coordinatorFeedbacks.stream()
                .anyMatch(f -> f.getStatus() == FeedbackStatus.REJECTED)) {
            return new DerivedWorkflowStatus(
                    ModuleVersionStatus.REJECTED_AT_COORDINATORS_FEEDBACK,
                    ProposalStatus.REJECTED_AT_COORDINATORS_FEEDBACK);
        }
        if (hasExam && examBoardAssigned.stream()
                .anyMatch(f -> f.getStatus() == FeedbackStatus.REJECTED)) {
            return new DerivedWorkflowStatus(
                    ModuleVersionStatus.REJECTED_AT_EXAMINATION_BOARD_FEEDBACK,
                    ProposalStatus.REJECTED_AT_EXAMINATION_BOARD_FEEDBACK);
        }

        if (hasCoordinator) {
            boolean coordinatorHasPending = coordinatorFeedbacks.stream()
                    .anyMatch(f -> f.getStatus() == FeedbackStatus.PENDING_FEEDBACK);
            if (coordinatorHasPending) {
                return new DerivedWorkflowStatus(
                        ModuleVersionStatus.PENDING_COORDINATORS_FEEDBACK,
                        ProposalStatus.PENDING_COORDINATORS_FEEDBACK);
            }
            boolean allCoordinatorsApproved = coordinatorFeedbacks.stream()
                    .allMatch(f -> f.getStatus() == FeedbackStatus.APPROVED);
            if (!allCoordinatorsApproved) {
                return new DerivedWorkflowStatus(
                        ModuleVersionStatus.COORDINATORS_FEEDBACK_GIVEN,
                        ProposalStatus.COORDINATORS_FEEDBACK_GIVEN);
            }
        }

        if (hasExam) {
            boolean examHasPending = examBoardAssigned.stream()
                    .anyMatch(f -> f.getStatus() == FeedbackStatus.PENDING_FEEDBACK);
            if (examHasPending) {
                return new DerivedWorkflowStatus(
                        ModuleVersionStatus.PENDING_EXAMINATION_BOARD_FEEDBACK,
                        ProposalStatus.PENDING_EXAMINATION_BOARD_FEEDBACK);
            }
            boolean allExamApproved = examBoardAssigned.stream()
                    .allMatch(f -> f.getStatus() == FeedbackStatus.APPROVED);
            if (allExamApproved) {
                return new DerivedWorkflowStatus(
                        ModuleVersionStatus.ACCEPTED,
                        ProposalStatus.ACCEPTED);
            }
            return new DerivedWorkflowStatus(
                    ModuleVersionStatus.EXAMINATION_BOARD_FEEDBACK_GIVEN,
                    ProposalStatus.EXAMINATION_BOARD_FEEDBACK_GIVEN);
        }

        return new DerivedWorkflowStatus(
                ModuleVersionStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION,
                ProposalStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION);
    }

    /** Active examination-board member rows selected for content-change invalidation. */
    public static List<Feedback> examinationBoardMemberFeedbacksToInvalidate(List<Feedback> activeFeedbacks) {
        if (activeFeedbacks == null || activeFeedbacks.isEmpty()) {
            return List.of();
        }
        return activeFeedbacks.stream()
                .filter(f -> !f.isInvalidated())
                .filter(ProposalWorkflowStatusDeriver::isExaminationBoardMemberFeedback)
                .toList();
    }

    /** All non-invalidated rows selected for assignment/step-1 invalidation. */
    public static List<Feedback> allActiveFeedbacksToInvalidate(List<Feedback> activeFeedbacks) {
        if (activeFeedbacks == null || activeFeedbacks.isEmpty()) {
            return List.of();
        }
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : activeFeedbacks) {
            if (f != null && !f.isInvalidated()) {
                result.add(f);
            }
        }
        return result;
    }
}
