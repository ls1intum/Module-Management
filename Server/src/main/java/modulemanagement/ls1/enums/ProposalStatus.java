package modulemanagement.ls1.enums;

public enum ProposalStatus {
    /**
     * Professor has not yet submitted for coordinator feedback (first submission).
     */
    PENDING_FIRST_SUBMISSION,
    /**
     * Submitted for coordinator feedback; waiting for program/area coordinators.
     */
    PENDING_COORDINATOR_FEEDBACK,
    /**
     * All coordinator responses are in; at least one gave non-approval feedback and
     * none approved yet.
     */
    COORDINATOR_FEEDBACK_GIVEN,
    /**
     * Coordinator feedback accepted; professor has not yet submitted for full
     * feedback.
     */
    PENDING_FULL_SUBMISSION,
    /** Submitted for full feedback; waiting for QM, advisor, examination board. */
    PENDING_FULL_FEEDBACK,
    ACCEPTED,
    REQUIRES_REVIEW,
    REJECTED,
    CANCELLED,
}
