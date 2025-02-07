package modulemanagement.ls1.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import lombok.Data;
import modulemanagement.ls1.models.ModuleVersion;

@Data
public class ModuleVersionUpdateRequestDTO {
    private Integer version;
    private String moduleId;
    private ModuleVersionStatus status;
    private Boolean isComplete;
    private String bulletPoints;
    private String titleEng;
    private String levelEng;
    private Language languageEng;
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


    @JsonIgnore
    public static ModuleVersionUpdateRequestDTO fromModuleVersion(ModuleVersion mv) {
        ModuleVersionUpdateRequestDTO mdto = new ModuleVersionUpdateRequestDTO();
        ModuleVersionToRequestDTO(mv, mdto);
        return mdto;
    }

    static void ModuleVersionToRequestDTO(ModuleVersion mv, ModuleVersionUpdateRequestDTO mdto) {
        mdto.setVersion(mv.getVersion());
        mdto.setModuleId(mv.getModuleId());
        mdto.setStatus(mv.getStatus());
        mdto.setIsComplete(mv.isCompleted());
        mdto.setBulletPoints(mv.getBulletPoints());
        mdto.setTitleEng(mv.getTitleEng());
        mdto.setLevelEng(mv.getLevelEng());
        mdto.setLanguageEng(mv.getLanguageEng());
        mdto.setFrequencyEng(mv.getFrequencyEng());
        mdto.setCredits(mv.getCredits());
        mdto.setDuration(mv.getDuration());
        mdto.setHoursTotal(mv.getHoursTotal());
        mdto.setHoursSelfStudy(mv.getHoursSelfStudy());
        mdto.setHoursPresence(mv.getHoursPresence());
        mdto.setExaminationAchievementsEng(mv.getExaminationAchievementsEng());
        mdto.setExaminationAchievementsPromptEng(mv.getExaminationAchievementsPromptEng());
        mdto.setRepetitionEng(mv.getRepetitionEng());
        mdto.setRecommendedPrerequisitesEng(mv.getRecommendedPrerequisitesEng());
        mdto.setContentEng(mv.getContentEng());
        mdto.setContentPromptEng(mv.getContentPromptEng());
        mdto.setLearningOutcomesEng(mv.getLearningOutcomesEng());
        mdto.setLearningOutcomesPromptEng(mv.getLearningOutcomesPromptEng());
        mdto.setTeachingMethodsEng(mv.getTeachingMethodsEng());
        mdto.setTeachingMethodsPromptEng(mv.getTeachingMethodsPromptEng());
        mdto.setMediaEng(mv.getMediaEng());
        mdto.setLiteratureEng(mv.getLiteratureEng());
        mdto.setResponsiblesEng(mv.getResponsiblesEng());
        mdto.setLvSwsLecturerEng(mv.getLvSwsLecturerEng());
    }
}