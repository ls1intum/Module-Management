package modulemanagement.ls1.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectFeedbackDTO {
    @NotNull
    private Long userId;
    @NotBlank
    private String comment;
}
