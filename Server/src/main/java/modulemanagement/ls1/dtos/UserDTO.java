package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.User;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;

    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

}
