package modulemanagement.ls1.modulemanagementserver.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserIdDTO {
    @NotNull private Long userId;
}
