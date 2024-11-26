package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserIdDTO {
    @NotNull private UUID userId;
}
