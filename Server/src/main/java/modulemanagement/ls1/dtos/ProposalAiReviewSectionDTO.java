package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.enums.AiReviewSeverity;
import modulemanagement.ls1.enums.ProposalReviewSection;

@Data
public class ProposalAiReviewSectionDTO {
    private ProposalReviewSection section;
    private String sectionLabel;
    private AiReviewSeverity severity;
    private String findings;
    private String suggestions;
}
