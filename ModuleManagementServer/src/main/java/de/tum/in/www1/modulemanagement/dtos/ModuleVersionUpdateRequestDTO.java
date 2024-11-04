package de.tum.in.www1.modulemanagement.dtos;

import de.tum.in.www1.modulemanagement.enums.Language;
import de.tum.in.www1.modulemanagement.enums.ModuleVersionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModuleVersionUpdateRequestDTO {
    @NotNull private Long userId;
    @NotNull private Long moduleVersionId;
    private Integer version;
    private String moduleId;
    private ModuleVersionStatus status;
    private Boolean isComplete;
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