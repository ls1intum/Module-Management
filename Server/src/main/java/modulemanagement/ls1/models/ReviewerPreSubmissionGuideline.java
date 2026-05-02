package modulemanagement.ls1.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.enums.UserRole;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "reviewer_pre_submission_guideline")
public class ReviewerPreSubmissionGuideline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guideline_id")
    private Long guidelineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_role", nullable = false, length = 64)
    @NotNull
    private UserRole reviewerRole;

    @Column(name = "title", nullable = false, length = 500)
    @NotNull
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "CLOB")
    @NotNull
    private String content;

    @Column(name = "good_example", columnDefinition = "CLOB")
    private String goodExample;

    @Column(name = "bad_example", columnDefinition = "CLOB")
    private String badExample;

    @Column(name = "related_module_field_key", length = 128)
    private String relatedModuleFieldKey;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
