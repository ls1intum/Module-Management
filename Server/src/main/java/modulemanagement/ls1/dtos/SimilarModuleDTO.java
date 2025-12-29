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
        dto.setLevelEng("");
        dto.setLanguageEng("");
        dto.setFrequencyEng("");
        dto.setCredits(0);
        dto.setDuration("");
        dto.setHoursTotal(0);
        dto.setHoursSelfStudy(0);
        dto.setHoursPresence(0);
        dto.setContentEng(toStringOrNull(module.get("content")));
        dto.setContentPromptEng("");
        dto.setLearningOutcomesEng(toStringOrNull(module.get("learning_outcomes")));
        dto.setLearningOutcomesPromptEng("");
        dto.setExaminationAchievementsEng(toStringOrNull(module.get("examination_achievements")));
        dto.setExaminationAchievementsPromptEng("");
        dto.setRepetitionEng("");
        dto.setRecommendedPrerequisitesEng("");
        dto.setTeachingMethodsEng(toStringOrNull(module.get("teaching_methods")));
        dto.setTeachingMethodsPromptEng("");
        dto.setMediaEng("");
        dto.setLiteratureEng(toStringOrNull(module.get("literature")));
        dto.setResponsiblesEng("");
        dto.setLvSwsLecturerEng("");
        dto.setSimilarity(similarity);
        return dto;
    }

    private static String toStringOrNull(Object value) {
        return value != null ? value.toString() : null;
    }
}
