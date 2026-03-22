package modulemanagement.ls1.dtos;

import jakarta.validation.Valid;
import modulemanagement.ls1.enums.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProposalRequestDTO {
    private String bulletPoints;
    @NotBlank
    private String titleEng;
    private String titleDe;
    private String levelEng;
    private Language languageEng;
    private String frequencyEng;
    private Integer credits;
    private Integer hoursLecture;
    private Integer hoursExercise;
    private Integer hoursPractical;
    private Integer hoursSeminar;
    private String firstSemesterAvailable;
    private String successorModuleName;
    @Valid
    private List<ModuleDegreeProgramAssignmentDTO> degreeProgramAssignments = new ArrayList<>();
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