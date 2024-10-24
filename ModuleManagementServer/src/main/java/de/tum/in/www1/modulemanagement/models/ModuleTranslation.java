package de.tum.in.www1.modulemanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "module_translation")
public class ModuleTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cit_module_id", nullable = false)
    @JsonIgnore
    private CitModule citModule;

    public enum Language {
        en, de
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Language language;

    @Column(name = "title")
    private String title;

    @Column(name = "recommended_prerequisites")
    private String recommendedPrerequisites;

    @Column(name = "assessment_method")
    private String assessmentMethod;

    @Column(name = "learning_outcomes")
    private String learningOutcomes;

    @Column(name = "media_used")
    private String mediaUsed;

    @Column(name = "literature")
    private String literature;
}

