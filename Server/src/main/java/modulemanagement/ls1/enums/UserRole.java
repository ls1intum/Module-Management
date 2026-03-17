package modulemanagement.ls1.enums;

public enum UserRole {
    ADMIN,
    QUALITY_MANAGEMENT,
    ACADEMIC_PROGRAM_ADVISOR,
    EXAMINATION_BOARD,
    PROFESSOR,
    /** User is responsible for at least one degree program; can receive and respond to feedback requests for those. */
    PROGRAM_COORDINATOR,
    /** User is coordinator for at least one area of specialization; can receive and respond to feedback requests for those. */
    SPECIALIZATION_AREA_COORDINATOR
}
