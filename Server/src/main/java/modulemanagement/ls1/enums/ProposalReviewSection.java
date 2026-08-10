package modulemanagement.ls1.enums;

/**
 * Proposal sections that AI review guidelines can target. Aligns with {@code ModuleVersion} fields
 * and reviewer feedback dimensions so the LLM can load only relevant rules per field.
 */
public enum ProposalReviewSection {
    GENERAL,
    TITLE_ENG,
    TITLE_DE,
    LEVEL_ENG,
    LANGUAGE_ENG,
    FREQUENCY_ENG,
    CREDITS,
    DURATION,
    HOURS_LECTURE,
    HOURS_EXERCISE,
    HOURS_PRACTICAL,
    HOURS_SEMINAR,
    FIRST_SEMESTER_AVAILABLE,
    SUCCESSOR_MODULE_NAME,
    HOURS_TOTAL,
    HOURS_SELF_STUDY,
    HOURS_PRESENCE,
    BULLET_POINTS,
    EXAMINATION_ACHIEVEMENTS,
    REPETITION,
    RECOMMENDED_PREREQUISITES,
    CONTENT,
    LEARNING_OUTCOMES,
    TEACHING_METHODS,
    MEDIA,
    LITERATURE,
    RESPONSIBLES,
    LV_SWS_LECTURER,
    DEGREE_PROGRAM_ASSIGNMENTS
}
