package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import modulemanagement.ls1.dtos.FeedbackDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private long feedbackId;

    @ManyToOne
    @JoinColumn(name = "feedback_from")
    private User feedbackFrom;

    @Column(name = "comment")
    private String Comment;

    @Column(name = "required_role")
    @Enumerated(EnumType.STRING)
    private UserRole requiredRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status")
    @NotNull private FeedbackStatus status;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @ManyToOne
    @JoinColumn(name = "module_version_id", nullable = false)
    @JsonIgnore
    private ModuleVersion moduleVersion;

    // --------- Module Version Fields -------------

    @Column(name = "title_feedback")
    private String titleFeedback;

    @Column(name = "title_accepted")
    private boolean titleAccepted;

    @Column(name = "level_feedback")
    private String levelFeedback;

    @Column(name = "level_accepted")
    private boolean levelAccepted;

    @Column(name = "language_feedback", columnDefinition = "CLOB")
    private String languageFeedback;

    @Column(name = "language_accepted")
    private boolean languageAccepted;

    @Column(name = "frequency_feedback", columnDefinition = "CLOB")
    private String frequencyFeedback;

    @Column(name = "frequency_accepted")
    private boolean frequencyAccepted;

    @Column(name = "credits_feedback", columnDefinition = "CLOB")
    private String creditsFeedback;

    @Column(name = "credits_accepted")
    private boolean creditsAccepted;

    @Column(name = "duration_feedback", columnDefinition = "CLOB")
    private String durationFeedback;

    @Column(name = "duration_accepted")
    private boolean durationAccepted;

    @Column(name = "hours_total_feedback", columnDefinition = "CLOB")
    private String hoursTotalFeedback;

    @Column(name = "hours_total_accepted")
    private boolean hoursTotalAccepted;

    @Column(name = "hours_self_study_feedback", columnDefinition = "CLOB")
    private String hoursSelfStudyFeedback;

    @Column(name = "hours_self_study_accepted")
    private boolean hoursSelfStudyAccepted;

    @Column(name = "hours_presence_feedback", columnDefinition = "CLOB")
    private String hoursPresenceFeedback;

    @Column(name = "hours_presence_accepted")
    private boolean hoursPresenceAccepted;

    @Column(name = "examination_feedback", columnDefinition = "CLOB")
    private String examinationAchievementsFeedback;

    @Column(name = "examination_accepted")
    private boolean examinationAchievementsAccepted;

    @Column(name = "repetition_feedback", columnDefinition = "CLOB")
    private String repetitionFeedback;

    @Column(name = "repetition_accepted")
    private boolean repetitionAccepted;

    @Column(name = "content_feedback", columnDefinition = "CLOB")
    private String contentFeedback;

    @Column(name = "content_accepted")
    private boolean contentAccepted;

    @Column(name = "learning_feedback", columnDefinition = "CLOB")
    private String learningOutcomesFeedback;

    @Column(name = "learning_accepted")
    private boolean learningOutcomesAccepted;

    @Column(name = "teaching_feedback", columnDefinition = "CLOB")
    private String teachingMethodsFeedback;

    @Column(name = "teaching_accepted")
    private boolean teachingMethodsAccepted;

    @Column(name = "media_feedback", columnDefinition = "CLOB")
    private String mediaFeedback;

    @Column(name = "media_accepted")
    private boolean mediaAccepted;

    @Column(name = "literature_feedback", columnDefinition = "CLOB")
    private String literatureFeedback;

    @Column(name = "literature_accepted")
    private boolean literatureAccepted;

    @Column(name = "responsibles_feedback", columnDefinition = "CLOB")
    private String responsiblesFeedback;

    @Column(name = "responsibles_accepted")
    private boolean responsiblesAccepted;

    @Column(name = "lv_feedback", columnDefinition = "CLOB")
    private String lvSwsLecturerFeedback;

    @Column(name = "lv_accepted")
    private boolean lvSwsLecturerAccepted;

    public boolean isRejected() {
        return this.getStatus() == FeedbackStatus.REJECTED;
    }

    public void insert(FeedbackDTO dto) {
        this.titleFeedback = dto.getTitleFeedback();
        this.titleAccepted = dto.isTitleAccepted();
        this.levelFeedback = dto.getLevelFeedback();
        this.levelAccepted = dto.isLevelAccepted();
        this.languageFeedback = dto.getLanguageFeedback();
        this.languageAccepted = dto.isLanguageAccepted();
        this.frequencyFeedback = dto.getFrequencyFeedback();
        this.frequencyAccepted = dto.isFrequencyAccepted();
        this.creditsFeedback = dto.getCreditsFeedback();
        this.creditsAccepted = dto.isCreditsAccepted();
        this.durationFeedback = dto.getDurationFeedback();
        this.durationAccepted = dto.isDurationAccepted();
        this.hoursTotalFeedback = dto.getHoursTotalFeedback();
        this.hoursTotalAccepted = dto.isHoursTotalAccepted();
        this.hoursSelfStudyFeedback = dto.getHoursSelfStudyFeedback();
        this.hoursSelfStudyAccepted = dto.isHoursSelfStudyAccepted();
        this.hoursPresenceFeedback = dto.getHoursPresenceFeedback();
        this.hoursPresenceAccepted = dto.isHoursPresenceAccepted();
        this.examinationAchievementsFeedback = dto.getExaminationAchievementsFeedback();
        this.examinationAchievementsAccepted = dto.isExaminationAchievementsAccepted();
        this.repetitionFeedback = dto.getRepetitionFeedback();
        this.repetitionAccepted = dto.isRepetitionAccepted();
        this.contentFeedback = dto.getContentFeedback();
        this.contentAccepted = dto.isContentAccepted();
        this.learningOutcomesFeedback = dto.getLearningOutcomesFeedback();
        this.learningOutcomesAccepted = dto.isLearningOutcomesAccepted();
        this.teachingMethodsFeedback = dto.getTeachingMethodsFeedback();
        this.teachingMethodsAccepted = dto.isTeachingMethodsAccepted();
        this.mediaFeedback = dto.getMediaFeedback();
        this.mediaAccepted = dto.isMediaAccepted();
        this.literatureFeedback = dto.getLiteratureFeedback();
        this.literatureAccepted = dto.isLiteratureAccepted();
        this.responsiblesFeedback = dto.getResponsiblesFeedback();
        this.responsiblesAccepted = dto.isResponsiblesAccepted();
        this.lvSwsLecturerFeedback = dto.getLvSwsLecturerFeedback();
        this.lvSwsLecturerAccepted = dto.isLvSwsLecturerAccepted();
    }

    public boolean isAllFeedbackPositive() {
        return this.titleAccepted
            && this.levelAccepted
            && this.languageAccepted
            && this.frequencyAccepted
            && this.creditsAccepted
            && this.durationAccepted
            && this.hoursTotalAccepted
            && this.hoursSelfStudyAccepted
            && this.hoursPresenceAccepted
            && this.examinationAchievementsAccepted
            && this.repetitionAccepted
            && this.contentAccepted
            && this.learningOutcomesAccepted
            && this.teachingMethodsAccepted
            && this.mediaAccepted
            && this.literatureAccepted
            && this.responsiblesAccepted
            && this.lvSwsLecturerAccepted;
    }
}
