package de.tum.in.www1.modulemanagement.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModuleVersionUpdateRequestDTO {
    @NotNull private Long userId;
    @NotNull private String titleEng;
    @NotNull private String levelEng;
    @NotNull private String languageEng;
    @NotNull private String frequencyEng;
    @NotNull private Integer credits;
    @NotNull private Integer hoursTotal;
    @NotNull private Integer hoursSelfStudy;
    @NotNull private Integer hoursPresence;
    @NotNull private String examinationAchievementsEng;
    @NotNull private String repetitionEng;
    @NotNull private String recommendedPrerequisitesEng;
    @NotNull private String contentEng;
    @NotNull private String learningOutcomesEng;
    @NotNull private String teachingMethodsEng;
    @NotNull private String mediaEng;
    @NotNull private String literatureEng;
    @NotNull private String responsiblesEng;
    @NotNull private String lvSwsLecturerEng;
}
