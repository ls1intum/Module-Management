package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import modulemanagement.ls1.enums.UserRole;

@Data
public class ReviewerPreSubmissionGuidelineWriteDto {
    @NotNull
    private UserRole reviewerRole;

    @NotBlank
    @Size(max = 500)
    private String title;

    @NotNull
    @Size(max = 100_000)
    private String content;

    @Size(max = 50_000)
    private String goodExample;

    @Size(max = 50_000)
    private String badExample;

    @Size(max = 128)
    private String relatedModuleFieldKey;

    private Integer sortOrder;

    private Boolean active;
}
