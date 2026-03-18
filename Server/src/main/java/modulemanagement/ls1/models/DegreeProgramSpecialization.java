package modulemanagement.ls1.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "degree_program_specialization")
public class DegreeProgramSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "degree_program_specialization_id")
    private Long degreeProgramSpecializationId;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private User responsibleUser;

    @ManyToMany(mappedBy = "degreeProgramSpecializations", fetch = FetchType.LAZY)
    private List<DegreeProgram> degreePrograms = new ArrayList<>();
}
