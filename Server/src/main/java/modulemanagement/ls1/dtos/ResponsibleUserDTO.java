package modulemanagement.ls1.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ResponsibleUserDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;

    public static ResponsibleUserDTO fromUser(modulemanagement.ls1.models.User user) {
        if (user == null) return null;
        ResponsibleUserDTO dto = new ResponsibleUserDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
