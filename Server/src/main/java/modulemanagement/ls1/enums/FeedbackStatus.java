package modulemanagement.ls1.enums;

public enum FeedbackStatus {
    PENDING_SUBMISSION,     // Corresponding ModuleVersion only created, but nothing to review yet.
    PENDING_FEEDBACK,       // Corresponding ModuleVersion is submitted, waiting for this feedback.
    APPROVED,               // Reviewer accepts this ModuleVersion.
    FEEDBACK_GIVEN,
    REJECTED,               // Reviewer rejects this ModuleVersion.
    OBSOLETE,               // A new ModuleVersion for this proposal was submitted.
    CANCELLED               // Review of corresponding ModuleVersion was cancelled.
}
