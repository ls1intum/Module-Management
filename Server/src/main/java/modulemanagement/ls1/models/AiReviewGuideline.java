package modulemanagement.ls1.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.enums.ProposalReviewSection;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ai_review_guideline")
public class AiReviewGuideline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guideline_id")
    private Long guidelineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "section", nullable = false)
    @NotNull
    private ProposalReviewSection section;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "instruction", nullable = false, columnDefinition = "CLOB")
    private String instruction;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
