package modulemanagement.ls1.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProposalAiReviewDTO {
    private Long moduleVersionId;
    private String summary;
    private List<ProposalAiReviewSectionDTO> sections = new ArrayList<>();
    private LocalDateTime generatedAt;
    /** False when no guidelines existed at generation time; review then relies on generic standards only. */
    private boolean guidelinesConfigured;
}
