package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.UserRole;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Roles that may use pending-feedback reviewer APIs
 * ({@link modulemanagement.ls1.controllers.FeedbackController})
 * and reviewer-only features such as pre-submission guidelines. Single source
 * of truth for this role set.
 */
public final class ReviewerRoles {

    /**
     * Same five roles everywhere we mean “reviewer / pending feedbacks staff”,
     * excluding {@link UserRole#ADMIN}.
     */
    public static final Set<UserRole> ROLES = EnumSet.of(
            UserRole.QUALITY_MANAGEMENT,
            UserRole.ACADEMIC_PROGRAM_ADVISOR,
            UserRole.EXAMINATION_BOARD,
            UserRole.PROGRAM_COORDINATOR,
            UserRole.SPECIALIZATION_AREA_COORDINATOR);

    /**
     * Quoted role names for {@code hasAnyRole(...)} SpEL. Must match
     * {@link #ROLES}; compile-time constant for
     * {@code @PreAuthorize} (cannot be built from {@link #ROLES} at runtime).
     */
    private static final String SPEL_REVIEWER_ROLE_NAMES = "'QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD', 'PROGRAM_COORDINATOR', 'SPECIALIZATION_AREA_COORDINATOR'";

    /**
     * SpEL for Spring {@code @PreAuthorize} on endpoints restricted to reviewers
     * only (e.g. feedbacks).
     */
    public static final String HAS_ANY_REVIEWER_ROLE = "hasAnyRole(" + SPEL_REVIEWER_ROLE_NAMES + ")";

    /**
     * SpEL for overlap / similarity checks: professors or any reviewer in
     * {@link #ROLES}.
     */
    public static final String HAS_PROFESSOR_OR_ANY_REVIEWER_ROLE = "hasAnyRole('PROFESSOR', "
            + SPEL_REVIEWER_ROLE_NAMES + ")";

   
    public static boolean isReviewerRole(UserRole role) {
        return role != null && ROLES.contains(role);
    }

    public static boolean userIsAdmin(List<UserRole> userRoles) {
        return userRoles != null && userRoles.contains(UserRole.ADMIN);
    }

    /** True if the user has at least one role in {@link #ROLES}. */
    public static boolean userHasAnyReviewerRole(List<UserRole> userRoles) {
        if (userRoles == null) {
            return false;
        }
        for (UserRole r : userRoles) {
            if (ROLES.contains(r)) {
                return true;
            }
        }
        return false;
    }
}
