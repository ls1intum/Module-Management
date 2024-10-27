package de.tum.in.www1.modulemanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleVersion> moduleVersions = new ArrayList<>();

    public List<ModuleVersion> addModuleVersion(ModuleVersion moduleVersion) {
        moduleVersions.add(moduleVersion);
        return moduleVersions;
    }

    @JsonIgnore
    public ModuleVersion getLatestModuleVersion() {
        return moduleVersions.stream().max(Comparator.comparing(ModuleVersion::getVersion)).orElse(null);
    }
}
