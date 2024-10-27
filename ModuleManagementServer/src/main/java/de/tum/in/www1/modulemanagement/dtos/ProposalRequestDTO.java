package de.tum.in.www1.modulemanagement.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProposalRequestDTO {
    @NotNull(message = "User ID is required.")
    private Long userId;
    private String titleEng;
    private String levelEng;
    private String languageEng;
    private String frequencyEng;
    private Integer credits;
    private Integer hoursTotal;
    private Integer hoursSelfStudy;
    private Integer hoursPresence;
    private String examinationAchievementsEng;
    private String repetitionEng;
    private String recommendedPrerequisiteEng;
    private String contentEng;
    private String learningOutcomesEng;
    private String teachingMethodsEng;
    private String mediaEng;
    private String literatureEng;
    private String responsiblesEng;
    private String lvSwsLecturerEng;

    @NotNull(message = "You need to state if you submit immediately of save.")
    private boolean submitImmediately;
}