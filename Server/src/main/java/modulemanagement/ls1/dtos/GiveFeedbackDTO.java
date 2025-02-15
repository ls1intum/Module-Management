package modulemanagement.ls1.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GiveFeedbackDTO {
    @NotBlank
    private String comment;
}
