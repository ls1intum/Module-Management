package modulemanagement.ls1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private long feedbackId;

    @ManyToOne
    @JoinColumn(name = "feedback_from")
    private User feedbackFrom;

    @Column(name = "comment")
    private String Comment;

    @Column(name = "required_role")
    @Enumerated(EnumType.STRING)
    private UserRole requiredRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status")
    @NotNull private FeedbackStatus status;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @ManyToOne
    @JoinColumn(name = "module_version_id", nullable = false)
    @JsonIgnore
    private ModuleVersion moduleVersion;
}
