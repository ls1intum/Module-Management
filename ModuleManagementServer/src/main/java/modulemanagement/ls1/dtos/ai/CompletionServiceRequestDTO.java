package modulemanagement.ls1.dtos.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import modulemanagement.ls1.enums.Language;

@Data
@Builder
public class CompletionServiceRequestDTO {
    @NotNull private String bulletPoints;
    private String titleEng;
    private String levelEng;
    private Language languageEng;
    private String frequencyEng;
    private Integer credits;
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
}
