package modulemanagement.ls1.enums;

public enum ProposalStatus {
    /**
     * Professor has not yet submitted for coordinator feedback (first submission).
     */
    WAITING_FOR_COORDINATORS_SUBMISSION,
    /**
     * Submitted for coordinator feedback; waiting for program/area coordinators.
     */
    PENDING_COORDINATORS_FEEDBACK,
    /**
     * All coordinator responses are in; at least one gave non-approval feedback and
     * none approved yet.
     */
    COORDINATORS_FEEDBACK_GIVEN,
    /**
     * Coordinator feedback accepted; professor has not yet submitted for
     * examination
     * board feedback.
     */
    WAITING_FOR_EXAMINATION_BOARD_SUBMISSION,
    /** Submitted for examination board feedback; waiting for examination board. */
    PENDING_EXAMINATION_BOARD_FEEDBACK,
    /**
     * Examination board responses are in; at least one is not approval (e.g.
     * FEEDBACK_GIVEN), or mixed.
     */
    EXAMINATION_BOARD_FEEDBACK_GIVEN,
    /** All examination board feedbacks approved. */
    ACCEPTED,
    /** Rejected during program/area coordinator feedback. */
    REJECTED_AT_COORDINATORS_FEEDBACK,
    /** Rejected during examination board feedback. */
    REJECTED_AT_EXAMINATION_BOARD_FEEDBACK,
    CANCELLED,
}
