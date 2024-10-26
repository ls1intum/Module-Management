package de.tum.in.www1.modulemanagement.models;

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

    @Column(name = "moduleId")
    private String moduleId;

    // -----------------------------
    @Column(name = "title_eng")
    private String titleEng;

    @Column(name = "title_de")
    private String titleDe;
    // -----------------------------

    @ManyToOne
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @OneToMany(mappedBy = "moduleVersion", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks;
}
