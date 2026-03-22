package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateExaminationBoardDTO {
    @NotBlank
    @Size(max = 512)
    private String name;
}
