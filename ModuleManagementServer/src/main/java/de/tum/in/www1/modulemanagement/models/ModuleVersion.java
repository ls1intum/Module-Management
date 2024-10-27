package de.tum.in.www1.modulemanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class ModuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long moduleVersionId;

    @Column(name = "version")
    private int version;

    @Column(name = "module_id")
    private String moduleId;

    @Column(name = "status")
    private String status;

    // --------MODULE_FIELDS----------
    @Column(name = "title_eng")
    private String titleEng;

    @Column(name = "level_eng")
    private String levelEng;

    @Column(name = "language_eng")
    private String languageEng;

    @Column(name = "frequency_eng")
    private String frequencyEng;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "hours_total")
    private Integer hoursTotal;

    @Column(name = "hours_self_study")
    private Integer hoursSelfStudy;

    @Column(name = "hours_presence")
    private Integer hoursPresence;

    @Column(name = "examination_achievements_eng", columnDefinition = "CLOB")
    private String examinationAchievementsEng;

    @Column(name = "repetition_eng")
    private String repetitionEng;

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "recommended_prerequisites_eng", columnDefinition = "CLOB")
    private String recommendedPrerequisitesEng;

    @Column(name = "content_eng", columnDefinition = "CLOB")
    private String contentEng;

    @Column(name = "learning_outcomes_eng", columnDefinition = "CLOB")
    private String learningOutcomesEng;

    @Column(name = "teaching_methods_eng", columnDefinition = "CLOB")
    private String teachingMethodsEng;

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

    @JsonIgnore
    public boolean isCompleted() {
        return !StringUtils.isEmpty(titleEng)
                && !StringUtils.isEmpty(levelEng)
                && !StringUtils.isEmpty(languageEng)
                && !StringUtils.isEmpty(frequencyEng)
                && !(credits == null)
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