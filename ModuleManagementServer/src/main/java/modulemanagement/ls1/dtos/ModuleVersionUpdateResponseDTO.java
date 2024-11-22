package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ModuleVersionUpdateResponseDTO extends ModuleVersionUpdateRequestDTO {
    @NotNull private Long proposalId;

}
