package de.tum.in.www1.modulemanagement.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cit_module")
public class CitModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "module_id", unique = true)
    private String moduleId;

    @OneToMany(mappedBy = "citModule", cascade = CascadeType.ALL)
    private List<ModuleTranslation> translations;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
}
