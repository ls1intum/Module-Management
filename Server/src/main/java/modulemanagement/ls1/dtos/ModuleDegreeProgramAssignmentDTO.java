package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModuleDegreeProgramAssignmentDTO {
    @NotNull
    private Long degreeProgramId;
    @NotNull
    private Long degreeProgramSpecializationId;
}
