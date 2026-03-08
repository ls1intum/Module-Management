package modulemanagement.ls1.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "module_version_degree_program_assignment",
       uniqueConstraints = @UniqueConstraint(columnNames = { "module_version_id", "degree_program_id" }))
public class ModuleVersionDegreeProgramAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_version_id", nullable = false)
    private ModuleVersion moduleVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private DegreeProgram degreeProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_program_specialization_id", nullable = false)
    private DegreeProgramSpecialization degreeProgramSpecialization;
}
