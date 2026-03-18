package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDegreeProgramSpecializationDTO {
    @NotBlank
    private String name;
    @NotNull
    private UUID responsibleUserId;
}
