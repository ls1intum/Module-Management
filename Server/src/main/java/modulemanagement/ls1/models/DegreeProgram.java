package modulemanagement.ls1.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "degree_program")
public class DegreeProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "degree_program_id")
    private Long degreeProgramId;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private User responsibleUser;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "degree_program_specialization_assignment", joinColumns = @JoinColumn(name = "degree_program_id"), inverseJoinColumns = @JoinColumn(name = "degree_program_specialization_id"))
    private List<DegreeProgramSpecialization> degreeProgramSpecializations = new ArrayList<>();

}
