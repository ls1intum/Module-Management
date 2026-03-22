package modulemanagement.ls1.enums;

public enum FeedbackStatus {
    PENDING_FEEDBACK, // Corresponding ModuleVersion is submitted, waiting for this feedback.
    APPROVED, // Reviewer accepts this ModuleVersion.
    FEEDBACK_GIVEN,
    REJECTED, // Reviewer rejects this ModuleVersion.
}
