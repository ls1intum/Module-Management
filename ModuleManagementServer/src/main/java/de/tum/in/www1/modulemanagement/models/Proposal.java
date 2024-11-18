package de.tum.in.www1.modulemanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tum.in.www1.modulemanagement.dtos.ProposalViewDTO;
import de.tum.in.www1.modulemanagement.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private ProposalStatus status;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleVersion> moduleVersions = new ArrayList<>();

    public List<ModuleVersion> addModuleVersion(ModuleVersion moduleVersion) {
        moduleVersions.add(moduleVersion);
        return moduleVersions;
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
    public ProposalViewDTO toProposalViewDTO() {
        var v = new ProposalViewDTO();
        v.setProposalId(proposalId);
        v.setLatestVersion(getLatestModuleVersion());
        v.setCreationDate(creationDate);
        v.setStatus(status);

        var sortedModuleVersions = moduleVersions.stream()
                .sorted(Comparator.comparing(ModuleVersion::getVersion).reversed())
                .toList();

        if (!sortedModuleVersions.isEmpty()) {
            v.setLatestModuleVersion(sortedModuleVersions.getFirst().toCompactDTO());

            var oldVersions = sortedModuleVersions.subList(1, sortedModuleVersions.size())
                    .stream()
                    .map(ModuleVersion::toCompactDTO)
                    .collect(Collectors.toList());

            v.setOldModuleVersions(oldVersions);
        } else {
            v.setLatestModuleVersion(null);
            v.setOldModuleVersions(Collections.emptyList());
        }

        return v;
    }

}
