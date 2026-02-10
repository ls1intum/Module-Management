package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

@Data
public class UserDTO {
    @NotNull
    private UUID userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private List<UserRole> roles = new ArrayList<>();

    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        return dto;
    }

}
