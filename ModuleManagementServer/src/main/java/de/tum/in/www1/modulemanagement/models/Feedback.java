package de.tum.in.www1.modulemanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tum.in.www1.modulemanagement.enums.FeedbackStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status")
    private FeedbackStatus status;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @ManyToOne
    @JoinColumn(name = "module_version_id", nullable = false)
    @JsonIgnore
    private ModuleVersion moduleVersion;
}
