package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;

import java.time.LocalDateTime;

@Data
public class ModuleVersionViewFeedbackDTO {
    @NotNull
    private Long feedbackId;
    private String feedbackFromFirstName;
    private String feedbackFromLastName;
    private String rejectionComment;
    private UserRole requiredRole;
    private String requestedFromUserName;
    private String requestedFromSpecializationName;
    private Long examinationBoardId;
    private String examinationBoardName;
    private FeedbackStatus feedbackStatus;
    private LocalDateTime createdAt;
    private LocalDateTime submissionDate;
    private Long degreeProgramSpecializationId;

    private String titleFeedback;
    private String titleDeFeedback;
    private String bulletPointsFeedback;
    private String levelFeedback;
    private String languageFeedback;
    private String frequencyFeedback;
    private String creditsFeedback;
    private String durationFeedback;
    private String hoursTotalFeedback;
    private String hoursSelfStudyFeedback;
    private String hoursPresenceFeedback;
    private String hoursLectureFeedback;
    private String hoursExerciseFeedback;
    private String hoursPracticalFeedback;
    private String hoursSeminarFeedback;
    private String firstSemesterAvailableFeedback;
    private String successorModuleNameFeedback;
    private String examinationAchievementsFeedback;
    private String repetitionFeedback;
    private String recommendedPrerequisitesFeedback;
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
        dto.setRequiredRole(f.getRequiredRole());
        if (f.getAssignedReviewer() != null) {
            var ar = f.getAssignedReviewer();
            dto.setRequestedFromUserName(ar.getFirstName() + " " + ar.getLastName());
        }
        if (f.getDegreeProgramSpecialization() != null) {
            var spec = f.getDegreeProgramSpecialization();
            dto.setRequestedFromSpecializationName(spec.getName());
            dto.setDegreeProgramSpecializationId(spec.getDegreeProgramSpecializationId());
            if (f.getAssignedReviewer() == null && spec.getResponsibleUser() != null) {
                var resp = spec.getResponsibleUser();
                dto.setRequestedFromUserName(resp.getFirstName() + " " + resp.getLastName());
            }
        }
        if (f.getExaminationBoard() != null) {
            dto.setExaminationBoardId(f.getExaminationBoard().getExaminationBoardId());
            dto.setExaminationBoardName(f.getExaminationBoard().getName());
        }
        dto.setFeedbackStatus(f.getStatus());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setSubmissionDate(f.getSubmissionDate());
        dto.setTitleFeedback(f.getTitleFeedback());
        dto.setTitleDeFeedback(f.getTitleDeFeedback());
        dto.setBulletPointsFeedback(f.getBulletPointsFeedback());
        dto.setLevelFeedback(f.getLevelFeedback());
        dto.setLanguageFeedback(f.getLanguageFeedback());
        dto.setFrequencyFeedback(f.getFrequencyFeedback());
        dto.setCreditsFeedback(f.getCreditsFeedback());
        dto.setDurationFeedback(f.getDurationFeedback());
        dto.setHoursTotalFeedback(f.getHoursTotalFeedback());
        dto.setHoursSelfStudyFeedback(f.getHoursSelfStudyFeedback());
        dto.setHoursPresenceFeedback(f.getHoursPresenceFeedback());
        dto.setHoursLectureFeedback(f.getHoursLectureFeedback());
        dto.setHoursExerciseFeedback(f.getHoursExerciseFeedback());
        dto.setHoursPracticalFeedback(f.getHoursPracticalFeedback());
        dto.setHoursSeminarFeedback(f.getHoursSeminarFeedback());
        dto.setFirstSemesterAvailableFeedback(f.getFirstSemesterAvailableFeedback());
        dto.setSuccessorModuleNameFeedback(f.getSuccessorModuleNameFeedback());
        dto.setExaminationAchievementsFeedback(f.getExaminationAchievementsFeedback());
        dto.setRepetitionFeedback(f.getRepetitionFeedback());
        dto.setRecommendedPrerequisitesFeedback(f.getRecommendedPrerequisitesFeedback());
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
