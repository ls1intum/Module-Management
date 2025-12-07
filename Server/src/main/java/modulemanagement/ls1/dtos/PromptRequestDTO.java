package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequestDTO {
    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;
}

