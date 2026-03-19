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

    @Column(name = "invalidated")
    private boolean invalidated;

    /**
     * When set, this feedback is for whoever is currently responsible for this
     * specialization (position-based).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "degree_program_specialization_id")
    @JsonIgnore
    private DegreeProgramSpecialization degreeProgramSpecialization;

    @Column(name = "comment")
    private String Comment;

    @Column(name = "required_role")
    @Enumerated(EnumType.STRING)
    private UserRole requiredRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status")
    @NotNull
    private FeedbackStatus status;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "module_version_id", nullable = false)
    @JsonIgnore
    private ModuleVersion moduleVersion;

    // --------- Module Version Fields -------------

    @Column(name = "title_feedback")
    private String titleFeedback;

    @Column(name = "title_accepted")
    private boolean titleAccepted;

    @Column(name = "title_de_feedback", columnDefinition = "CLOB")
    private String titleDeFeedback;

    @Column(name = "title_de_accepted")
    private boolean titleDeAccepted;

    @Column(name = "bullet_points_feedback", columnDefinition = "CLOB")
    private String bulletPointsFeedback;

    @Column(name = "bullet_points_accepted")
    private boolean bulletPointsAccepted;

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

    @Column(name = "hours_lecture_feedback", columnDefinition = "CLOB")
    private String hoursLectureFeedback;

    @Column(name = "hours_lecture_accepted")
    private boolean hoursLectureAccepted;

    @Column(name = "hours_exercise_feedback", columnDefinition = "CLOB")
    private String hoursExerciseFeedback;

    @Column(name = "hours_exercise_accepted")
    private boolean hoursExerciseAccepted;

    @Column(name = "hours_practical_feedback", columnDefinition = "CLOB")
    private String hoursPracticalFeedback;

    @Column(name = "hours_practical_accepted")
    private boolean hoursPracticalAccepted;

    @Column(name = "hours_seminar_feedback", columnDefinition = "CLOB")
    private String hoursSeminarFeedback;

    @Column(name = "hours_seminar_accepted")
    private boolean hoursSeminarAccepted;

    @Column(name = "first_semester_available_feedback", columnDefinition = "CLOB")
    private String firstSemesterAvailableFeedback;

    @Column(name = "first_semester_available_accepted")
    private boolean firstSemesterAvailableAccepted;

    @Column(name = "successor_module_name_feedback", columnDefinition = "CLOB")
    private String successorModuleNameFeedback;

    @Column(name = "successor_module_name_accepted")
    private boolean successorModuleNameAccepted;

    @Column(name = "examination_feedback", columnDefinition = "CLOB")
    private String examinationAchievementsFeedback;

    @Column(name = "examination_accepted")
    private boolean examinationAchievementsAccepted;

    @Column(name = "examination_prompt_feedback", columnDefinition = "CLOB")
    private String examinationAchievementsPromptFeedback;

    @Column(name = "examination_prompt_accepted")
    private boolean examinationAchievementsPromptAccepted;

    @Column(name = "repetition_feedback", columnDefinition = "CLOB")
    private String repetitionFeedback;

    @Column(name = "repetition_accepted")
    private boolean repetitionAccepted;

    @Column(name = "recommended_prerequisites_feedback", columnDefinition = "CLOB")
    private String recommendedPrerequisitesFeedback;

    @Column(name = "recommended_prerequisites_accepted")
    private boolean recommendedPrerequisitesAccepted;

    @Column(name = "content_feedback", columnDefinition = "CLOB")
    private String contentFeedback;

    @Column(name = "content_accepted")
    private boolean contentAccepted;

    @Column(name = "content_prompt_feedback", columnDefinition = "CLOB")
    private String contentPromptFeedback;

    @Column(name = "content_prompt_accepted")
    private boolean contentPromptAccepted;

    @Column(name = "learning_feedback", columnDefinition = "CLOB")
    private String learningOutcomesFeedback;

    @Column(name = "learning_accepted")
    private boolean learningOutcomesAccepted;

    @Column(name = "learning_prompt_feedback", columnDefinition = "CLOB")
    private String learningOutcomesPromptFeedback;

    @Column(name = "learning_prompt_accepted")
    private boolean learningOutcomesPromptAccepted;

    @Column(name = "teaching_feedback", columnDefinition = "CLOB")
    private String teachingMethodsFeedback;

    @Column(name = "teaching_accepted")
    private boolean teachingMethodsAccepted;

    @Column(name = "teaching_prompt_feedback", columnDefinition = "CLOB")
    private String teachingMethodsPromptFeedback;

    @Column(name = "teaching_prompt_accepted")
    private boolean teachingMethodsPromptAccepted;

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

    public boolean isFeedbackGiven() {
        return this.getStatus() == FeedbackStatus.FEEDBACK_GIVEN;
    }

    public void insert(FeedbackDTO dto) {
        this.titleFeedback = dto.getTitleFeedback();
        this.titleAccepted = dto.isTitleAccepted();
        this.titleDeFeedback = dto.getTitleDeFeedback();
        this.titleDeAccepted = dto.isTitleDeAccepted();
        this.bulletPointsFeedback = dto.getBulletPointsFeedback();
        this.bulletPointsAccepted = dto.isBulletPointsAccepted();
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
        this.hoursLectureFeedback = dto.getHoursLectureFeedback();
        this.hoursLectureAccepted = dto.isHoursLectureAccepted();
        this.hoursExerciseFeedback = dto.getHoursExerciseFeedback();
        this.hoursExerciseAccepted = dto.isHoursExerciseAccepted();
        this.hoursPracticalFeedback = dto.getHoursPracticalFeedback();
        this.hoursPracticalAccepted = dto.isHoursPracticalAccepted();
        this.hoursSeminarFeedback = dto.getHoursSeminarFeedback();
        this.hoursSeminarAccepted = dto.isHoursSeminarAccepted();
        this.firstSemesterAvailableFeedback = dto.getFirstSemesterAvailableFeedback();
        this.firstSemesterAvailableAccepted = dto.isFirstSemesterAvailableAccepted();
        this.successorModuleNameFeedback = dto.getSuccessorModuleNameFeedback();
        this.successorModuleNameAccepted = dto.isSuccessorModuleNameAccepted();
        this.examinationAchievementsFeedback = dto.getExaminationAchievementsFeedback();
        this.examinationAchievementsAccepted = dto.isExaminationAchievementsAccepted();
        this.examinationAchievementsPromptFeedback = dto.getExaminationAchievementsPromptFeedback();
        this.examinationAchievementsPromptAccepted = dto.isExaminationAchievementsPromptAccepted();
        this.repetitionFeedback = dto.getRepetitionFeedback();
        this.repetitionAccepted = dto.isRepetitionAccepted();
        this.recommendedPrerequisitesFeedback = dto.getRecommendedPrerequisitesFeedback();
        this.recommendedPrerequisitesAccepted = dto.isRecommendedPrerequisitesAccepted();
        this.contentFeedback = dto.getContentFeedback();
        this.contentAccepted = dto.isContentAccepted();
        this.contentPromptFeedback = dto.getContentPromptFeedback();
        this.contentPromptAccepted = dto.isContentPromptAccepted();
        this.learningOutcomesFeedback = dto.getLearningOutcomesFeedback();
        this.learningOutcomesAccepted = dto.isLearningOutcomesAccepted();
        this.learningOutcomesPromptFeedback = dto.getLearningOutcomesPromptFeedback();
        this.learningOutcomesPromptAccepted = dto.isLearningOutcomesPromptAccepted();
        this.teachingMethodsFeedback = dto.getTeachingMethodsFeedback();
        this.teachingMethodsAccepted = dto.isTeachingMethodsAccepted();
        this.teachingMethodsPromptFeedback = dto.getTeachingMethodsPromptFeedback();
        this.teachingMethodsPromptAccepted = dto.isTeachingMethodsPromptAccepted();
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
                && this.titleDeAccepted
                && this.bulletPointsAccepted
                && this.levelAccepted
                && this.languageAccepted
                && this.frequencyAccepted
                && this.creditsAccepted
                && this.durationAccepted
                && this.hoursTotalAccepted
                && this.hoursSelfStudyAccepted
                && this.hoursPresenceAccepted
                && this.hoursLectureAccepted
                && this.hoursExerciseAccepted
                && this.hoursPracticalAccepted
                && this.hoursSeminarAccepted
                && this.firstSemesterAvailableAccepted
                && this.successorModuleNameAccepted
                && this.examinationAchievementsAccepted
                && this.examinationAchievementsPromptAccepted
                && this.repetitionAccepted
                && this.recommendedPrerequisitesAccepted
                && this.contentAccepted
                && this.contentPromptAccepted
                && this.learningOutcomesAccepted
                && this.learningOutcomesPromptAccepted
                && this.teachingMethodsAccepted
                && this.teachingMethodsPromptAccepted
                && this.mediaAccepted
                && this.literatureAccepted
                && this.responsiblesAccepted
                && this.lvSwsLecturerAccepted;
    }
}
