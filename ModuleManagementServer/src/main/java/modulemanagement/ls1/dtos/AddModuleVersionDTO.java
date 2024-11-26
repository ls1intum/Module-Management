package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddModuleVersionDTO {
    @NotNull private UUID userId;
    @NotNull private Long proposalId;

}
