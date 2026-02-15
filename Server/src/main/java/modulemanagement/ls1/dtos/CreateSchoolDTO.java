package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSchoolDTO {

    @NotBlank(message = "School name is required")
    private String name;
}
