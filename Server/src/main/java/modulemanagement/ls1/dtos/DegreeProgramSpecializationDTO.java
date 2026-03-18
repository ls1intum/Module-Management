package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.models.DegreeProgramSpecialization;

@Data
public class DegreeProgramSpecializationDTO {
    @NotNull
    private Long degreeProgramSpecializationId;
    @NotNull
    private String name;
    @NotNull
    private ResponsibleUserDTO responsibleUser;

    public static DegreeProgramSpecializationDTO fromEntity(DegreeProgramSpecialization entity) {
        DegreeProgramSpecializationDTO dto = new DegreeProgramSpecializationDTO();
        dto.setDegreeProgramSpecializationId(entity.getDegreeProgramSpecializationId());
        dto.setName(entity.getName());
        if (entity.getResponsibleUser() != null) {
            dto.setResponsibleUser(ResponsibleUserDTO.fromUser(entity.getResponsibleUser()));
        }
        return dto;
    }
}
