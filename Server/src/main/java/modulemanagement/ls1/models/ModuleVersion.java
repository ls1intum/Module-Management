package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class ModuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long moduleVersionId;

    @Column(name = "version")
    private Integer version;

    @Column(name = "module_id")
    private String moduleId;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @NotNull
    private ModuleVersionStatus status;

    @Column(name = "bullet_points", columnDefinition = "CLOB")
    private String bulletPoints;

    // --------MODULE_FIELDS----------
    @Column(name = "title_eng")
    private String titleEng;

    @Column(name = "title_de")
    private String titleDe;

    @Column(name = "hours_lecture")
    private Integer hoursLecture;

    @Column(name = "hours_exercise")
    private Integer hoursExercise;

    @Column(name = "hours_practical")
    private Integer hoursPractical;

    @Column(name = "hours_seminar")
    private Integer hoursSeminar;

    @Column(name = "first_semester_available")
    private String firstSemesterAvailable;

    @Column(name = "successor_module_name")
    private String successorModuleName;

    @Column(name = "level_eng")
    private String levelEng;

    @Column(name = "language_eng")
    @Enumerated(EnumType.STRING)
    private Language languageEng;

    @Column(name = "frequency_eng")
    private String frequencyEng;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "duration")
    private String duration;

    @Column(name = "hours_total")
    private Integer hoursTotal;

    @Column(name = "hours_self_study")
    private Integer hoursSelfStudy;

    @Column(name = "hours_presence")
    private Integer hoursPresence;

    @Column(name = "examination_achievements_eng", columnDefinition = "CLOB")
    private String examinationAchievementsEng;

    @Column(name = "examination_achievements_prompt_eng", columnDefinition = "CLOB")
    private String examinationAchievementsPromptEng;

    @Column(name = "repetition_eng")
    private String repetitionEng;

    @Column(name = "recommended_prerequisites_eng", columnDefinition = "CLOB")
    private String recommendedPrerequisitesEng;

    @Column(name = "content_eng", columnDefinition = "CLOB")
    private String contentEng;

    @Column(name = "content_prompt_eng", columnDefinition = "CLOB")
    private String contentPromptEng;

    @Column(name = "learning_outcomes_eng", columnDefinition = "CLOB")
    private String learningOutcomesEng;

    @Column(name = "learning_outcomes_prompt_eng", columnDefinition = "CLOB")
    private String learningOutcomesPromptEng;

    @Column(name = "teaching_methods_eng", columnDefinition = "CLOB")
    private String teachingMethodsEng;

    @Column(name = "teaching_methods_prompt_eng", columnDefinition = "CLOB")
    private String teachingMethodsPromptEng;

    @Column(name = "media_eng", columnDefinition = "CLOB")
    private String mediaEng;

    @Column(name = "literature_eng", columnDefinition = "CLOB")
    private String literatureEng;

    @Column(name = "responsibles_eng", columnDefinition = "CLOB")
    private String responsiblesEng;

    @Column(name = "lv_sws_lecturer_eng", columnDefinition = "CLOB")
    private String lvSwsLecturerEng;

    // -----------------------------

    @ManyToOne
    @JoinColumn(name = "proposal_id", nullable = false)
    @JsonIgnore
    private Proposal proposal;

    @OneToMany(mappedBy = "moduleVersion", cascade = CascadeType.ALL)
    private List<Feedback> requiredFeedbacks;

    @OneToMany(mappedBy = "moduleVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleVersionDegreeProgramAssignment> degreeProgramAssignments = new ArrayList<>();

    @JsonIgnore
    public boolean isFirstStepComplete() {
        if (StringUtils.isEmpty(titleEng) || StringUtils.isEmpty(titleDe))
            return false;
        if (credits == null)
            return false;
        if (StringUtils.isEmpty(frequencyEng))
            return false;
        if (hoursLecture == null || hoursExercise == null || hoursPractical == null || hoursSeminar == null)
            return false;
        if (StringUtils.isEmpty(firstSemesterAvailable) || StringUtils.isEmpty(successorModuleName))
            return false;
        if (languageEng == null)
            return false;
        if (degreeProgramAssignments == null || degreeProgramAssignments.isEmpty())
            return false;
        return degreeProgramAssignments.stream()
                .allMatch(a -> a.getDegreeProgramSpecialization() != null && a.getDegreeProgram() != null);
    }

    @JsonIgnore
    public boolean isCompleted() {
        return !StringUtils.isEmpty(titleEng)
                && !StringUtils.isEmpty(levelEng)
                && languageEng != null
                && !StringUtils.isEmpty(frequencyEng)
                && !(credits == null)
                && !StringUtils.isEmpty(duration)
                && !(hoursTotal == null)
                && !(hoursSelfStudy == null)
                && !(hoursPresence == null)
                && !StringUtils.isEmpty(examinationAchievementsEng)
                && !StringUtils.isEmpty(repetitionEng)
                && !StringUtils.isEmpty(recommendedPrerequisitesEng)
                && !StringUtils.isEmpty(contentEng)
                && !StringUtils.isEmpty(learningOutcomesEng)
                && !StringUtils.isEmpty(teachingMethodsEng)
                && !StringUtils.isEmpty(mediaEng)
                && !StringUtils.isEmpty(literatureEng)
                && !StringUtils.isEmpty(responsiblesEng)
                && !StringUtils.isEmpty(lvSwsLecturerEng);
    }

}
