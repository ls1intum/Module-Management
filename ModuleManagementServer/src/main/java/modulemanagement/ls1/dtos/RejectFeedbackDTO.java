package modulemanagement.ls1.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectFeedbackDTO {
    @NotBlank
    private String comment;
}
