package modulemanagement.ls1.dtos;

import modulemanagement.ls1.enums.Language;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProposalRequestDTO {
    private String bulletPoints;
    @NotNull private String titleEng;
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