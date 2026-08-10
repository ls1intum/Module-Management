package modulemanagement.ls1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.enums.AiReviewSeverity;
import modulemanagement.ls1.enums.ProposalReviewSection;

@Data
@NoArgsConstructor
@Entity
@Table(name = "proposal_ai_review_section")
public class ProposalAiReviewSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProposalAiReview review;

    @Enumerated(EnumType.STRING)
    @Column(name = "section", nullable = false)
    @NotNull
    private ProposalReviewSection section;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    @NotNull
    private AiReviewSeverity severity;

    @Column(name = "findings", columnDefinition = "CLOB")
    private String findings;

    @Column(name = "suggestions", columnDefinition = "CLOB")
    private String suggestions;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
