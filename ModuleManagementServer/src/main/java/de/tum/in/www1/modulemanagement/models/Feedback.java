package de.tum.in.www1.modulemanagement.models;

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
    @JoinColumn(name = "feedback_from", nullable = false)
    private User feedbackFrom;

    @Column(name = "comment")
    private String Comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status")
    private FeedbackStatus status;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;



}
