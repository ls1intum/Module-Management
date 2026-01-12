package modulemanagement.ls1.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarModuleDTO {
    private String moduleId;
    private String titleEng;
    private String levelEng;
    // Don't use language in case of different languages of existing modules
    private String languageEng;
    private String frequencyEng;
    private Integer credits;
    private String duration;
    private Integer hoursTotal;
    private Integer hoursSelfStudy;
    private Integer hoursPresence;
    private String examinationAchievementsEng;
    private String examinationAchievementsPromptEng;
    private String repetitionEng;
    private String recommendedPrerequisitesEng;
    private String contentEng;
    private String contentPromptEng;
    private String learningOutcomesEng;
    private String learningOutcomesPromptEng;
    private String teachingMethodsEng;
    private String teachingMethodsPromptEng;
    private String mediaEng;
    private String literatureEng;
    private String responsiblesEng;
    private String lvSwsLecturerEng;
    private double similarity;

    public static SimilarModuleDTO fromModuleMap(Map<String, Object> module, double similarity) {
        SimilarModuleDTO dto = new SimilarModuleDTO();
        dto.setModuleId(toStringOrNull(module.get("module_id")));
        dto.setTitleEng(toStringOrNull(module.get("title")));
        dto.setLevelEng(toStringOrNull(module.get("level")));
        dto.setLanguageEng(toStringOrNull(module.get("language")));
        dto.setFrequencyEng(toStringOrNull(module.get("frequency")));
        dto.setCredits(toIntOrZero(module.get("credits")));
        dto.setDuration(toStringOrNull(module.get("semester_duration")));
        dto.setHoursTotal(toIntOrZero(module.get("hours_total")));
        dto.setHoursSelfStudy(toIntOrZero(module.get("hours_self_study")));
        dto.setHoursPresence(toIntOrZero(module.get("hours_presence")));
        dto.setContentEng(toStringOrNull(module.get("content")));
        dto.setContentPromptEng("");
        dto.setLearningOutcomesEng(toStringOrNull(module.get("learning_outcomes")));
        dto.setLearningOutcomesPromptEng("");
        dto.setExaminationAchievementsEng(toStringOrNull(module.get("examination_achievements")));
        dto.setExaminationAchievementsPromptEng("");
        dto.setRepetitionEng(toStringOrNull(module.get("repetition")));
        dto.setRecommendedPrerequisitesEng(toStringOrNull(module.get("recommended_prerequisites")));
        dto.setTeachingMethodsEng(toStringOrNull(module.get("teaching_methods")));
        dto.setTeachingMethodsPromptEng("");
        dto.setMediaEng(toStringOrNull(module.get("media")));
        dto.setLiteratureEng(toStringOrNull(module.get("literature")));
        dto.setResponsiblesEng("");
        dto.setLvSwsLecturerEng(toStringOrNull(module.get("lv_sws_lecturer")));
        dto.setSimilarity(similarity);
        return dto;
    }

    private static String toStringOrNull(Object value) {
        return value != null ? value.toString() : null;
    }

    private static Integer toIntOrZero(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
