package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import modulemanagement.ls1.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Enumerated(EnumType.STRING)
    @NotNull
    private ProposalStatus status;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleVersion> moduleVersions = new ArrayList<>();

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
        newMv.setStatus(latestMv.getStatus());
        newMv.setModuleId(latestMv.getModuleId());
        newMv.setBulletPoints(latestMv.getBulletPoints());
        newMv.setTitleEng(latestMv.getTitleEng());
        newMv.setTitleDe(latestMv.getTitleDe());
        newMv.setHoursLecture(latestMv.getHoursLecture());
        newMv.setHoursExercise(latestMv.getHoursExercise());
        newMv.setHoursPractical(latestMv.getHoursPractical());
        newMv.setHoursSeminar(latestMv.getHoursSeminar());
        newMv.setFirstSemesterAvailable(latestMv.getFirstSemesterAvailable());
        newMv.setSuccessorModuleName(latestMv.getSuccessorModuleName());
        newMv.setLevelEng(latestMv.getLevelEng());
        newMv.setLanguageEng(latestMv.getLanguageEng());
        newMv.setFrequencyEng(latestMv.getFrequencyEng());
        newMv.setCredits(latestMv.getCredits());
        newMv.setDuration(latestMv.getDuration());
        newMv.setHoursTotal(latestMv.getHoursTotal());
        newMv.setHoursSelfStudy(latestMv.getHoursSelfStudy());
        newMv.setHoursPresence(latestMv.getHoursPresence());
        newMv.setExaminationAchievementsEng(latestMv.getExaminationAchievementsEng());
        newMv.setExaminationAchievementsPromptEng(latestMv.getExaminationAchievementsPromptEng());
        newMv.setRepetitionEng(latestMv.getRepetitionEng());
        newMv.setRecommendedPrerequisitesEng(latestMv.getRecommendedPrerequisitesEng());
        newMv.setContentEng(latestMv.getContentEng());
        newMv.setContentPromptEng(latestMv.getContentPromptEng());
        newMv.setLearningOutcomesEng(latestMv.getLearningOutcomesEng());
        newMv.setLearningOutcomesPromptEng(latestMv.getLearningOutcomesPromptEng());
        newMv.setTeachingMethodsEng(latestMv.getTeachingMethodsEng());
        newMv.setTeachingMethodsPromptEng(latestMv.getTeachingMethodsPromptEng());
        newMv.setMediaEng(latestMv.getMediaEng());
        newMv.setLiteratureEng(latestMv.getLiteratureEng());
        newMv.setResponsiblesEng(latestMv.getResponsiblesEng());
        newMv.setLvSwsLecturerEng(latestMv.getLvSwsLecturerEng());

        if (latestMv.getDegreeProgramAssignments() != null) {
            for (var a : latestMv.getDegreeProgramAssignments()) {
                ModuleVersionDegreeProgramAssignment newAssig = new ModuleVersionDegreeProgramAssignment();
                newAssig.setModuleVersion(newMv);
                newAssig.setDegreeProgram(a.getDegreeProgram());
                newAssig.setDegreeProgramSpecialization(a.getDegreeProgramSpecialization());
                newMv.getDegreeProgramAssignments().add(newAssig);
            }
        }
        newMv.setRequiredFeedbacks(new ArrayList<>());
        moduleVersions.add(newMv);
    }

}
