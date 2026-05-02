package modulemanagement.ls1.shared;

import java.util.Set;

/**
 * Allowed {@code relatedModuleFieldKey} values for reviewer pre-submission guidelines:
 * main proposal/module fields from {@link modulemanagement.ls1.dtos.ModuleVersionViewDTO}, excluding LLM prompt
 * companion fields ({@code *PromptEng}).
 */
public final class ModuleVersionGuidelineFieldKeys {
    private static final Set<String> KEYS = Set.of(
            "bulletPoints",
            "titleEng",
            "titleDe",
            "levelEng",
            "languageEng",
            "frequencyEng",
            "credits",
            "hoursLecture",
            "hoursExercise",
            "hoursPractical",
            "hoursSeminar",
            "firstSemesterAvailable",
            "successorModuleName",
            "duration",
            "hoursTotal",
            "hoursSelfStudy",
            "hoursPresence",
            "examinationAchievementsEng",
            "repetitionEng",
            "recommendedPrerequisitesEng",
            "contentEng",
            "learningOutcomesEng",
            "teachingMethodsEng",
            "mediaEng",
            "literatureEng",
            "responsiblesEng",
            "lvSwsLecturerEng");

    public static boolean isAllowed(String key) {
        return key != null && KEYS.contains(key);
    }
}
