package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;

import java.time.LocalDateTime;

@Data
public class ModuleVersionViewFeedbackDTO {
    @NotNull private Long feedbackId;
    private String feedbackFromFirstName;
    private String feedbackFromLastName;
    private String rejectionComment;
    private UserRole feedbackRole;
    private FeedbackStatus feedbackStatus;
    private LocalDateTime submissionDate;

    private String titleFeedback;
    private String levelFeedback;
    private String languageFeedback;
    private String frequencyFeedback;
    private String creditsFeedback;
    private String durationFeedback;
    private String hoursTotalFeedback;
    private String hoursSelfStudyFeedback;
    private String hoursPresenceFeedback;
    private String examinationAchievementsFeedback;
    private String repetitionFeedback;
    private String contentFeedback;
    private String learningOutcomesFeedback;
    private String teachingMethodsFeedback;
    private String mediaFeedback;
    private String literatureFeedback;
    private String responsiblesFeedback;
    private String lvSwsLecturerFeedback;

    public static ModuleVersionViewFeedbackDTO from(Feedback f) {
        var dto = new ModuleVersionViewFeedbackDTO();
        dto.setFeedbackId(f.getFeedbackId());
        if (f.getFeedbackFrom() != null) {
            dto.setFeedbackFromFirstName(f.getFeedbackFrom().getFirstName());
            dto.setFeedbackFromLastName(f.getFeedbackFrom().getLastName());
        }
        dto.setRejectionComment(f.getComment());
        dto.setFeedbackRole(f.getRequiredRole());
        dto.setFeedbackStatus(f.getStatus());
        dto.setSubmissionDate(f.getSubmissionDate());
        dto.setTitleFeedback(f.getTitleFeedback());
        dto.setLevelFeedback(f.getLevelFeedback());
        dto.setLanguageFeedback(f.getLanguageFeedback());
        dto.setFrequencyFeedback(f.getFrequencyFeedback());
        dto.setCreditsFeedback(f.getCreditsFeedback());
        dto.setDurationFeedback(f.getDurationFeedback());
        dto.setHoursTotalFeedback(f.getHoursTotalFeedback());
        dto.setHoursSelfStudyFeedback(f.getHoursSelfStudyFeedback());
        dto.setHoursPresenceFeedback(f.getHoursPresenceFeedback());
        dto.setExaminationAchievementsFeedback(f.getExaminationAchievementsFeedback());
        dto.setRepetitionFeedback(f.getRepetitionFeedback());
        dto.setContentFeedback(f.getContentFeedback());
        dto.setLearningOutcomesFeedback(f.getLearningOutcomesFeedback());
        dto.setTeachingMethodsFeedback(f.getTeachingMethodsFeedback());
        dto.setMediaFeedback(f.getMediaFeedback());
        dto.setLiteratureFeedback(f.getLiteratureFeedback());
        dto.setResponsiblesFeedback(f.getResponsiblesFeedback());
        dto.setLvSwsLecturerFeedback(f.getLvSwsLecturerFeedback());
        return dto;
    }
}
