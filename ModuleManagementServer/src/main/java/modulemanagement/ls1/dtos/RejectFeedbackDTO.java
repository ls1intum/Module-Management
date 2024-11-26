package modulemanagement.ls1.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RejectFeedbackDTO {
    @NotNull
    private UUID userId;
    @NotBlank
    private String comment;
}
