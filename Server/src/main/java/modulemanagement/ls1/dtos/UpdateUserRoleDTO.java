package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.enums.UserRole;

import java.util.List;

@Data
public class UpdateUserRoleDTO {
    @NotNull
    private List<UserRole> roles;
}
