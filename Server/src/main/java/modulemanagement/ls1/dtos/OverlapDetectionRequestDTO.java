package modulemanagement.ls1.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.models.ModuleVersion;

@Data
@NoArgsConstructor
public class OverlapDetectionRequestDTO {
    private String contextPrompt;
    private String titleEng;
    private String levelEng;
    private Language languageEng;
    private String frequencyEng;
    private Integer credits;
    private String durationEng;
    private Integer hoursTotal;
    private Integer hoursSelfStudy;
    private Integer hoursPresence;
    private String examinationAchievementsEng;
    private String repetitionEng;
    private String recommendedPrerequisitesEng;
    private String contentEng;
    private String learningOutcomesEng;
    private String teachingMethodsEng;
    private String mediaEng;
    private String literatureEng;
    private String responsiblesEng;
    private String lvSwsLecturerEng;

    public static OverlapDetectionRequestDTO from(ModuleVersion mv) {
        OverlapDetectionRequestDTO dto = new OverlapDetectionRequestDTO();
        dto.contextPrompt = mv.getBulletPoints();
        dto.titleEng = mv.getTitleEng();
        dto.levelEng = mv.getLevelEng();
        dto.languageEng = mv.getLanguageEng();
        dto.frequencyEng = mv.getFrequencyEng();
        dto.credits = mv.getCredits();
        dto.durationEng = mv.getDuration();
        dto.hoursTotal = mv.getHoursTotal();
        dto.hoursSelfStudy = mv.getHoursSelfStudy();
        dto.hoursPresence = mv.getHoursPresence();
        dto.examinationAchievementsEng = mv.getExaminationAchievementsEng();
        dto.repetitionEng = mv.getRepetitionEng();
        dto.recommendedPrerequisitesEng = mv.getRecommendedPrerequisitesEng();
        dto.contentEng = mv.getContentEng();
        dto.learningOutcomesEng = mv.getLearningOutcomesEng();
        dto.teachingMethodsEng = mv.getTeachingMethodsEng();
        dto.mediaEng = mv.getMediaEng();
        dto.literatureEng = mv.getLiteratureEng();
        dto.responsiblesEng = mv.getResponsiblesEng();
        dto.lvSwsLecturerEng = mv.getLvSwsLecturerEng();
        return dto;
    }
}
