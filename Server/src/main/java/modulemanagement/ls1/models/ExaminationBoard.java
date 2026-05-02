package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "examination_board")
public class ExaminationBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "examination_board_id")
    private Long examinationBoardId;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "examination_board_user",
            joinColumns = @JoinColumn(name = "examination_board_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private Set<User> members = new HashSet<>();
}
