package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ModuleVersionViewDTO {
    private Long proposalId;
    private Long moduleVersionId;
    private Integer version;
    private LocalDateTime creationDate;
    private ModuleVersionStatus status;

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
    private List<ModuleVersionViewFeedbackDTO> feedbacks;

    public static ModuleVersionViewDTO from(ModuleVersion moduleVersion) {
        ModuleVersionViewDTO dto = new ModuleVersionViewDTO();
        dto.proposalId = moduleVersion.getProposal().getProposalId();
        dto.moduleVersionId = moduleVersion.getModuleVersionId();
        dto.version = moduleVersion.getVersion();
        dto.creationDate = moduleVersion.getCreationDate();
        dto.status = moduleVersion.getStatus();
        dto.titleEng = moduleVersion.getTitleEng();
        dto.levelEng = moduleVersion.getLevelEng();
        dto.languageEng = moduleVersion.getLanguageEng();
        dto.frequencyEng = moduleVersion.getFrequencyEng();
        dto.credits = moduleVersion.getCredits();
        dto.hoursTotal = moduleVersion.getHoursTotal();
        dto.hoursSelfStudy = moduleVersion.getHoursSelfStudy();
        dto.hoursPresence = moduleVersion.getHoursPresence();
        dto.examinationAchievementsEng = moduleVersion.getExaminationAchievementsEng();
        dto.repetitionEng = moduleVersion.getRepetitionEng();
        dto.recommendedPrerequisitesEng = moduleVersion.getRecommendedPrerequisitesEng();
        dto.contentEng = moduleVersion.getContentEng();
        dto.learningOutcomesEng = moduleVersion.getLearningOutcomesEng();
        dto.teachingMethodsEng = moduleVersion.getTeachingMethodsEng();
        dto.mediaEng = moduleVersion.getMediaEng();
        dto.literatureEng = moduleVersion.getLiteratureEng();
        dto.responsiblesEng = moduleVersion.getResponsiblesEng();
        dto.lvSwsLecturerEng = moduleVersion.getLvSwsLecturerEng();

        List<ModuleVersionViewFeedbackDTO> feedbacks = new ArrayList<>();
        for (Feedback f : moduleVersion.getRequiredFeedbacks()) {
            feedbacks.add(ModuleVersionViewFeedbackDTO.from(f));
        }
        dto.feedbacks = feedbacks;

        return dto;
    }
}
