package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddModuleVersionDTO {
    @NotNull private Long proposalId;

}
