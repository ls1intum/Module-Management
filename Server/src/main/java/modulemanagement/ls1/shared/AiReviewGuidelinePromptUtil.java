package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.ProposalReviewSection;
import modulemanagement.ls1.models.AiReviewGuideline;
import modulemanagement.ls1.models.ModuleVersion;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Maps proposal sections to module fields and formats stored guidelines for LLM review prompts.
 */
public final class AiReviewGuidelinePromptUtil {

    private static final Map<ProposalReviewSection, String> SECTION_LABELS = Map.ofEntries(
            Map.entry(ProposalReviewSection.GENERAL, "General (whole proposal)"),
            Map.entry(ProposalReviewSection.TITLE_ENG, "Title (English)"),
            Map.entry(ProposalReviewSection.TITLE_DE, "Title (German)"),
            Map.entry(ProposalReviewSection.LEVEL_ENG, "Level"),
            Map.entry(ProposalReviewSection.LANGUAGE_ENG, "Language"),
            Map.entry(ProposalReviewSection.FREQUENCY_ENG, "Frequency"),
            Map.entry(ProposalReviewSection.CREDITS, "Credits"),
            Map.entry(ProposalReviewSection.DURATION, "Duration"),
            Map.entry(ProposalReviewSection.HOURS_LECTURE, "Hours (Lecture)"),
            Map.entry(ProposalReviewSection.HOURS_EXERCISE, "Hours (Exercise)"),
            Map.entry(ProposalReviewSection.HOURS_PRACTICAL, "Hours (Practical)"),
            Map.entry(ProposalReviewSection.HOURS_SEMINAR, "Hours (Seminar)"),
            Map.entry(ProposalReviewSection.FIRST_SEMESTER_AVAILABLE, "First semester available"),
            Map.entry(ProposalReviewSection.SUCCESSOR_MODULE_NAME, "Successor module"),
            Map.entry(ProposalReviewSection.HOURS_TOTAL, "Total hours"),
            Map.entry(ProposalReviewSection.HOURS_SELF_STUDY, "Self-study hours"),
            Map.entry(ProposalReviewSection.HOURS_PRESENCE, "Presence hours"),
            Map.entry(ProposalReviewSection.BULLET_POINTS, "Key points"),
            Map.entry(ProposalReviewSection.EXAMINATION_ACHIEVEMENTS, "Examination achievements"),
            Map.entry(ProposalReviewSection.REPETITION, "Repetition"),
            Map.entry(ProposalReviewSection.RECOMMENDED_PREREQUISITES, "Recommended prerequisites"),
            Map.entry(ProposalReviewSection.CONTENT, "Module content"),
            Map.entry(ProposalReviewSection.LEARNING_OUTCOMES, "Learning outcomes"),
            Map.entry(ProposalReviewSection.TEACHING_METHODS, "Teaching methods"),
            Map.entry(ProposalReviewSection.MEDIA, "Media"),
            Map.entry(ProposalReviewSection.LITERATURE, "Literature"),
            Map.entry(ProposalReviewSection.RESPONSIBLES, "Responsibles"),
            Map.entry(ProposalReviewSection.LV_SWS_LECTURER, "Lecturer"),
            Map.entry(ProposalReviewSection.DEGREE_PROGRAM_ASSIGNMENTS, "Degree program assignments"));

    private static final Map<ProposalReviewSection, Function<ModuleVersion, String>> SECTION_VALUE_EXTRACTORS =
            new EnumMap<>(ProposalReviewSection.class);

    static {
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.TITLE_ENG, ModuleVersion::getTitleEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.TITLE_DE, ModuleVersion::getTitleDe);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.LEVEL_ENG, ModuleVersion::getLevelEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.LANGUAGE_ENG,
                mv -> mv.getLanguageEng() != null ? mv.getLanguageEng().name() : null);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.FREQUENCY_ENG, ModuleVersion::getFrequencyEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.CREDITS, mv -> integerToString(mv.getCredits()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.DURATION, ModuleVersion::getDuration);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_LECTURE, mv -> integerToString(mv.getHoursLecture()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_EXERCISE, mv -> integerToString(mv.getHoursExercise()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_PRACTICAL, mv -> integerToString(mv.getHoursPractical()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_SEMINAR, mv -> integerToString(mv.getHoursSeminar()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.FIRST_SEMESTER_AVAILABLE, ModuleVersion::getFirstSemesterAvailable);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.SUCCESSOR_MODULE_NAME, ModuleVersion::getSuccessorModuleName);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_TOTAL, mv -> integerToString(mv.getHoursTotal()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_SELF_STUDY, mv -> integerToString(mv.getHoursSelfStudy()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.HOURS_PRESENCE, mv -> integerToString(mv.getHoursPresence()));
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.BULLET_POINTS, ModuleVersion::getBulletPoints);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.EXAMINATION_ACHIEVEMENTS, ModuleVersion::getExaminationAchievementsEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.REPETITION, ModuleVersion::getRepetitionEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.RECOMMENDED_PREREQUISITES, ModuleVersion::getRecommendedPrerequisitesEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.CONTENT, ModuleVersion::getContentEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.LEARNING_OUTCOMES, ModuleVersion::getLearningOutcomesEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.TEACHING_METHODS, ModuleVersion::getTeachingMethodsEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.MEDIA, ModuleVersion::getMediaEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.LITERATURE, ModuleVersion::getLiteratureEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.RESPONSIBLES, ModuleVersion::getResponsiblesEng);
        SECTION_VALUE_EXTRACTORS.put(ProposalReviewSection.LV_SWS_LECTURER, ModuleVersion::getLvSwsLecturerEng);
    }

    private AiReviewGuidelinePromptUtil() {
    }

    public static String getSectionLabel(ProposalReviewSection section) {
        return SECTION_LABELS.getOrDefault(section, section.name());
    }

    public static String extractFieldValue(ModuleVersion moduleVersion, ProposalReviewSection section) {
        if (moduleVersion == null || section == null) {
            return null;
        }
        Function<ModuleVersion, String> extractor = SECTION_VALUE_EXTRACTORS.get(section);
        if (extractor == null) {
            return null;
        }
        String value = extractor.apply(moduleVersion);
        return value != null && !value.isBlank() ? value : null;
    }

    public static Map<ProposalReviewSection, List<AiReviewGuideline>> groupBySection(List<AiReviewGuideline> guidelines) {
        if (guidelines == null || guidelines.isEmpty()) {
            return Map.of();
        }
        return guidelines.stream().collect(Collectors.groupingBy(AiReviewGuideline::getSection, Collectors.toList()));
    }

    /**
     * Formats guidelines for a single section (including {@link ProposalReviewSection#GENERAL} when passed).
     */
    public static String formatGuidelinesForSection(List<AiReviewGuideline> guidelines, ProposalReviewSection section) {
        if (guidelines == null || guidelines.isEmpty() || section == null) {
            return "";
        }
        List<AiReviewGuideline> matching = guidelines.stream()
                .filter(g -> g.getSection() == section)
                .toList();
        if (matching.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Review guidelines for ").append(getSectionLabel(section)).append(":\n");
        int index = 1;
        for (AiReviewGuideline guideline : matching) {
            sb.append(index++).append(". ").append(guideline.getTitle()).append("\n");
            sb.append(guideline.getInstruction().trim()).append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * Builds a section-scoped review prompt block: field value plus GENERAL and section-specific guidelines.
     */
    public static String buildSectionReviewContext(ModuleVersion moduleVersion, ProposalReviewSection section,
            List<AiReviewGuideline> allGuidelines) {
        List<String> parts = new ArrayList<>();
        String fieldValue = extractFieldValue(moduleVersion, section);
        if (fieldValue != null) {
            parts.add(getSectionLabel(section) + " (submitted value):\n" + fieldValue);
        }
        String generalRules = formatGuidelinesForSection(allGuidelines, ProposalReviewSection.GENERAL);
        if (!generalRules.isEmpty()) {
            parts.add(generalRules);
        }
        if (section != ProposalReviewSection.GENERAL) {
            String sectionRules = formatGuidelinesForSection(allGuidelines, section);
            if (!sectionRules.isEmpty()) {
                parts.add(sectionRules);
            }
        }
        return String.join("\n\n", parts);
    }

    private static String integerToString(Integer value) {
        return value != null ? value.toString() : null;
    }
}
