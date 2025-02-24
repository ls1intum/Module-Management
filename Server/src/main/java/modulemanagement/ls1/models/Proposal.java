package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.services.ProposalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proposal_id")
    private long proposalId;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "status")
    @NotNull private ProposalStatus status;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleVersion> moduleVersions = new ArrayList<>();

    @JsonIgnore
    public void addModuleVersion(ModuleVersion moduleVersion) {
        moduleVersions.add(moduleVersion);
    }

    @JsonIgnore
    public ModuleVersion getLatestModuleVersionWithContent() {
        return moduleVersions.stream().max(Comparator.comparing(ModuleVersion::getVersion)).orElse(null);
    }

    @JsonIgnore
    public Integer getLatestModuleVersion() {
        return getLatestModuleVersionWithContent().getVersion();
    }


    @JsonIgnore
    public void addNewModuleVersion() {
        ModuleVersion latestMv = getLatestModuleVersionWithContent();
        ModuleVersion newMv = new ModuleVersion();
        newMv.setProposal(this);
        newMv.setVersion(latestMv.getVersion() + 1);
        newMv.setCreationDate(LocalDateTime.now());
        newMv.setStatus(ModuleVersionStatus.PENDING_SUBMISSION);
        newMv.setBulletPoints(latestMv.getBulletPoints());
        newMv.setTitleEng(latestMv.getTitleEng());
        newMv.setLevelEng(latestMv.getLevelEng());
        newMv.setLanguageEng(latestMv.getLanguageEng());
        newMv.setFrequencyEng(latestMv.getFrequencyEng());
        newMv.setCredits(latestMv.getCredits());
        newMv.setDuration(latestMv.getDuration());
        newMv.setHoursTotal(latestMv.getHoursTotal());
        newMv.setHoursSelfStudy(latestMv.getHoursSelfStudy());
        newMv.setHoursPresence(latestMv.getHoursPresence());
        newMv.setExaminationAchievementsEng(latestMv.getExaminationAchievementsEng());
        newMv.setRepetitionEng(latestMv.getRepetitionEng());
        newMv.setRecommendedPrerequisitesEng(latestMv.getRecommendedPrerequisitesEng());
        newMv.setContentEng(latestMv.getContentEng());
        newMv.setLearningOutcomesEng(latestMv.getLearningOutcomesEng());
        newMv.setTeachingMethodsEng(latestMv.getTeachingMethodsEng());
        newMv.setMediaEng(latestMv.getMediaEng());
        newMv.setLiteratureEng(latestMv.getLiteratureEng());
        newMv.setResponsiblesEng(latestMv.getResponsiblesEng());
        newMv.setLvSwsLecturerEng(latestMv.getLvSwsLecturerEng());

        List<Feedback> requiredFeedbacks = new ArrayList<>();
        ProposalService.createNewFeedbacks(newMv, requiredFeedbacks);
        newMv.setRequiredFeedbacks(requiredFeedbacks);
        addModuleVersion(newMv);
        this.setStatus(ProposalStatus.PENDING_SUBMISSION);
    }

    public List<ModuleVersionViewFeedbackDTO> getPreviousModuleVersionFeedback() {
        List<ModuleVersion> mvs = this.getModuleVersions();
        Collections.reverse(mvs);
        for (ModuleVersion mv : mvs) {
            if (mv.isFeedbackGiven()) {
                List<ModuleVersionViewFeedbackDTO> previousFeedbacks = new ArrayList<>();
                List<Feedback> feedbacks = mv.getRequiredFeedbacks();
                for (Feedback feedback : feedbacks) {
                    if (feedback.isFeedbackGiven()) {
                        previousFeedbacks.add(ModuleVersionViewFeedbackDTO.from(feedback));
                    }
                }
                return previousFeedbacks;
            }
        }
        return Collections.emptyList();
    }
}
