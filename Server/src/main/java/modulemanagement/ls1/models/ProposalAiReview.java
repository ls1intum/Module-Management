package modulemanagement.ls1.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persisted result of an AI proposal review. One row per module version;
 * regenerating replaces the previous result.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "proposal_ai_review", uniqueConstraints = @UniqueConstraint(columnNames = { "module_version_id" }))
public class ProposalAiReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_version_id", nullable = false)
    private ModuleVersion moduleVersion;

    @Column(name = "summary", columnDefinition = "CLOB")
    private String summary;

    /** Whether any guidelines existed when this review was generated. */
    @Column(name = "guidelines_configured", nullable = false)
    private boolean guidelinesConfigured;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProposalAiReviewSection> sections = new ArrayList<>();
}
