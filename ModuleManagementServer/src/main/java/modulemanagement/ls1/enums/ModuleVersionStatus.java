package modulemanagement.ls1.modulemanagementserver.enums;

public enum ModuleVersionStatus {
    PENDING_SUBMISSION,     // Not submitted yet.
    PENDING_FEEDBACK,       // Submitted, and review is requested.
    ACCEPTED,               // All feedbacks accept.
    REJECTED,               // One feedback rejects.
    OBSOLETE,               // New ModuleVersion was proposed.
    CANCELLED,              // Proposal of this version was cancelled.
}
