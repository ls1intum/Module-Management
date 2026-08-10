package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import modulemanagement.ls1.enums.ProposalReviewSection;

@Data
public class CreateAiReviewGuidelineDTO {
    @NotNull
    private ProposalReviewSection section;

    @NotBlank
    @Size(max = 256)
    private String title;

    @NotBlank
    private String instruction;

    private Integer sortOrder;
}
