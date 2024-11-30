package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import modulemanagement.ls1.dtos.ModuleVersionCompactDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @NotNull private ModuleVersionStatus status;

    // --------MODULE_FIELDS----------
    @Column(name = "title_eng")
    private String titleEng;

    @Column(name = "level_eng")
    private String levelEng;

    @Column(name = "language_eng")
    @Enumerated(EnumType.STRING)
    private Language languageEng;

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
                && languageEng != Language.undefined
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

    @JsonIgnore
    public ModuleVersionCompactDTO toCompactDTO() {
        var dto = new ModuleVersionCompactDTO();
        dto.setModuleVersionId(this.moduleVersionId);
        dto.setVersion(this.version);
        dto.setTitleEng(this.titleEng);
        dto.setStatus(this.status);
        dto.setIsComplete(this.isCompleted());
        dto.setFeedbackList(this.requiredFeedbacks);
        return dto;
    }

    @JsonIgnore
    public ModuleVersionUpdateRequestDTO toModuleUpdateRequestDTO() {
        ModuleVersionUpdateRequestDTO mdto = new ModuleVersionUpdateRequestDTO();
        toModuleVersionDto(mdto);
        return mdto;
    }

    public ModuleVersionUpdateResponseDTO toModuleUpdateResponseDTO() {
        ModuleVersionUpdateResponseDTO mdto = new ModuleVersionUpdateResponseDTO();
        toModuleVersionDto(mdto);
        mdto.setProposalId(this.proposal.getProposalId());
        return mdto;
    }

    private void toModuleVersionDto(ModuleVersionUpdateRequestDTO mdto) {
        mdto.setVersion(this.version);
        mdto.setModuleId(this.moduleId);
        mdto.setStatus(this.status);
        mdto.setIsComplete(this.isCompleted());
        mdto.setTitleEng(this.titleEng);
        mdto.setLevelEng(this.levelEng);
        mdto.setLanguageEng(this.languageEng);
        mdto.setFrequencyEng(this.frequencyEng);
        mdto.setCredits(this.credits);
        mdto.setHoursTotal(this.hoursTotal);
        mdto.setHoursSelfStudy(this.hoursSelfStudy);
        mdto.setHoursPresence(this.hoursPresence);
        mdto.setExaminationAchievementsEng(this.examinationAchievementsEng);
        mdto.setRepetitionEng(this.repetitionEng);
        mdto.setRecommendedPrerequisitesEng(this.recommendedPrerequisitesEng);
        mdto.setContentEng(this.contentEng);
        mdto.setLearningOutcomesEng(this.learningOutcomesEng);
        mdto.setTeachingMethodsEng(this.teachingMethodsEng);
        mdto.setMediaEng(this.mediaEng);
        mdto.setLiteratureEng(this.literatureEng);
        mdto.setResponsiblesEng(this.responsiblesEng);
        mdto.setLvSwsLecturerEng(this.lvSwsLecturerEng);
    }
}
