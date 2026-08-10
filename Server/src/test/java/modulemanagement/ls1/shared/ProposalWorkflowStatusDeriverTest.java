package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.enums.ProposalStatus;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.ExaminationBoard;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProposalWorkflowStatusDeriverTest {

    @Test
    void ignoresInvalidatedFeedbackWhenDerivingStatus() {
        Feedback rejectedButInvalidated = coordinator(FeedbackStatus.REJECTED);
        rejectedButInvalidated.setInvalidated(true);
        Feedback pending = coordinator(FeedbackStatus.PENDING_FEEDBACK);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(rejectedButInvalidated, pending));

        assertTrue(derived.isPresent());
        assertEquals(ModuleVersionStatus.PENDING_COORDINATORS_FEEDBACK, derived.get().moduleVersionStatus());
        assertEquals(ProposalStatus.PENDING_COORDINATORS_FEEDBACK, derived.get().proposalStatus());
    }

    @Test
    void returnsEmptyWhenOnlyInvalidatedFeedbackRemains() {
        Feedback invalidated = coordinator(FeedbackStatus.APPROVED);
        invalidated.setInvalidated(true);

        assertTrue(ProposalWorkflowStatusDeriver.derive(List.of(invalidated)).isEmpty());
        assertTrue(ProposalWorkflowStatusDeriver.derive(List.of()).isEmpty());
        assertTrue(ProposalWorkflowStatusDeriver.derive(null).isEmpty());
    }

    @Test
    void coordinatorRejectionTakesPrecedenceOverExaminationBoardApproval() {
        Feedback coordRejected = coordinator(FeedbackStatus.REJECTED);
        Feedback examApproved = examBoard(FeedbackStatus.APPROVED);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(coordRejected, examApproved));

        assertEquals(ModuleVersionStatus.REJECTED_AT_COORDINATORS_FEEDBACK,
                derived.orElseThrow().moduleVersionStatus());
        assertEquals(ProposalStatus.REJECTED_AT_COORDINATORS_FEEDBACK,
                derived.orElseThrow().proposalStatus());
    }

    @Test
    void examinationBoardRejectionAppliesWhenCoordinatorsApproved() {
        Feedback coordApproved = coordinator(FeedbackStatus.APPROVED);
        Feedback examRejected = examBoard(FeedbackStatus.REJECTED);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(coordApproved, examRejected));

        assertEquals(ModuleVersionStatus.REJECTED_AT_EXAMINATION_BOARD_FEEDBACK,
                derived.orElseThrow().moduleVersionStatus());
    }

    @Test
    void pendingCoordinatorKeepsCoordinatorPhase() {
        Feedback pending = coordinator(FeedbackStatus.PENDING_FEEDBACK);
        Feedback approved = coordinator(FeedbackStatus.APPROVED);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(pending, approved));

        assertEquals(ModuleVersionStatus.PENDING_COORDINATORS_FEEDBACK,
                derived.orElseThrow().moduleVersionStatus());
    }

    @Test
    void mixedCoordinatorOutcomesYieldCoordinatorsFeedbackGiven() {
        Feedback approved = coordinator(FeedbackStatus.APPROVED);
        Feedback given = coordinator(FeedbackStatus.FEEDBACK_GIVEN);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(approved, given));

        assertEquals(ModuleVersionStatus.COORDINATORS_FEEDBACK_GIVEN,
                derived.orElseThrow().moduleVersionStatus());
        assertEquals(ProposalStatus.COORDINATORS_FEEDBACK_GIVEN,
                derived.orElseThrow().proposalStatus());
    }

    @Test
    void allCoordinatorsApprovedWithoutExamWaitsForExaminationBoardSubmission() {
        Feedback approved1 = coordinator(FeedbackStatus.APPROVED);
        Feedback approved2 = coordinator(FeedbackStatus.APPROVED);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(approved1, approved2));

        assertEquals(ModuleVersionStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION,
                derived.orElseThrow().moduleVersionStatus());
        assertEquals(ProposalStatus.WAITING_FOR_EXAMINATION_BOARD_SUBMISSION,
                derived.orElseThrow().proposalStatus());
    }

    @Test
    void pendingExaminationBoardAfterCoordinatorApproval() {
        Feedback coordApproved = coordinator(FeedbackStatus.APPROVED);
        Feedback examPending = examBoard(FeedbackStatus.PENDING_FEEDBACK);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(coordApproved, examPending));

        assertEquals(ModuleVersionStatus.PENDING_EXAMINATION_BOARD_FEEDBACK,
                derived.orElseThrow().moduleVersionStatus());
    }

    @Test
    void unanimousExaminationBoardApprovalAcceptsProposal() {
        Feedback coordApproved = coordinator(FeedbackStatus.APPROVED);
        Feedback exam1 = examBoard(FeedbackStatus.APPROVED);
        Feedback exam2 = examBoard(FeedbackStatus.APPROVED);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(coordApproved, exam1, exam2));

        assertEquals(ModuleVersionStatus.ACCEPTED, derived.orElseThrow().moduleVersionStatus());
        assertEquals(ProposalStatus.ACCEPTED, derived.orElseThrow().proposalStatus());
    }

    @Test
    void mixedExaminationBoardOutcomesYieldExaminationBoardFeedbackGiven() {
        Feedback coordApproved = coordinator(FeedbackStatus.APPROVED);
        Feedback examApproved = examBoard(FeedbackStatus.APPROVED);
        Feedback examGiven = examBoard(FeedbackStatus.FEEDBACK_GIVEN);

        var derived = ProposalWorkflowStatusDeriver.derive(List.of(coordApproved, examApproved, examGiven));

        assertEquals(ModuleVersionStatus.EXAMINATION_BOARD_FEEDBACK_GIVEN,
                derived.orElseThrow().moduleVersionStatus());
        assertEquals(ProposalStatus.EXAMINATION_BOARD_FEEDBACK_GIVEN,
                derived.orElseThrow().proposalStatus());
    }

    @Test
    void step1InvalidationSelectsAllActiveFeedback() {
        Feedback coord = coordinator(FeedbackStatus.APPROVED);
        Feedback exam = examBoard(FeedbackStatus.PENDING_FEEDBACK);
        Feedback alreadyInvalid = coordinator(FeedbackStatus.APPROVED);
        alreadyInvalid.setInvalidated(true);

        List<Feedback> selected = ProposalWorkflowStatusDeriver
                .allActiveFeedbacksToInvalidate(List.of(coord, exam, alreadyInvalid));

        assertEquals(2, selected.size());
        assertTrue(selected.contains(coord));
        assertTrue(selected.contains(exam));
        assertFalse(selected.contains(alreadyInvalid));
    }

    @Test
    void contentChangeInvalidationSelectsOnlyExaminationBoardMemberFeedback() {
        Feedback coord = coordinator(FeedbackStatus.APPROVED);
        Feedback exam = examBoard(FeedbackStatus.APPROVED);
        Feedback examInvalid = examBoard(FeedbackStatus.PENDING_FEEDBACK);
        examInvalid.setInvalidated(true);

        List<Feedback> selected = ProposalWorkflowStatusDeriver
                .examinationBoardMemberFeedbacksToInvalidate(List.of(coord, exam, examInvalid));

        assertEquals(1, selected.size());
        assertSame(exam, selected.get(0));
    }

    @Test
    void classifiesCoordinatorAndExaminationBoardRows() {
        Feedback coord = coordinator(FeedbackStatus.PENDING_FEEDBACK);
        Feedback exam = examBoard(FeedbackStatus.PENDING_FEEDBACK);

        assertTrue(ProposalWorkflowStatusDeriver.isCoordinatorFeedback(coord));
        assertFalse(ProposalWorkflowStatusDeriver.isExaminationBoardMemberFeedback(coord));
        assertTrue(ProposalWorkflowStatusDeriver.isExaminationBoardMemberFeedback(exam));
        assertFalse(ProposalWorkflowStatusDeriver.isCoordinatorFeedback(exam));
    }

    private static Feedback coordinator(FeedbackStatus status) {
        Feedback f = new Feedback();
        f.setStatus(status);
        f.setInvalidated(false);
        f.setAssignedReviewer(user());
        DegreeProgramSpecialization spec = new DegreeProgramSpecialization();
        spec.setDegreeProgramSpecializationId(1L);
        spec.setName("Databases");
        f.setDegreeProgramSpecialization(spec);
        f.setExaminationBoard(null);
        f.setRequiredRole(null);
        return f;
    }

    private static Feedback examBoard(FeedbackStatus status) {
        Feedback f = new Feedback();
        f.setStatus(status);
        f.setInvalidated(false);
        f.setAssignedReviewer(user());
        ExaminationBoard board = new ExaminationBoard();
        board.setExaminationBoardId(10L);
        board.setName("INF Board");
        f.setExaminationBoard(board);
        f.setDegreeProgramSpecialization(null);
        f.setRequiredRole(null);
        return f;
    }

    private static User user() {
        User u = new User();
        u.setUserId(UUID.randomUUID());
        u.setUserName("reviewer");
        return u;
    }
}
