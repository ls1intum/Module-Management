package modulemanagement.ls1.dtos.OverlapDetection;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.enums.Language;

import java.util.ArrayList;
import java.util.List;

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
}

