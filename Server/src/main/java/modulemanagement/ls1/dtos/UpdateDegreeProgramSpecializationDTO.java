package modulemanagement.ls1.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateDegreeProgramSpecializationDTO {
    private String name;
    private UUID responsibleUserId;
}
