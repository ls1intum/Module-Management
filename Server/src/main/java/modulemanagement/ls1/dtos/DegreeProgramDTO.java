package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.models.DegreeProgram;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class DegreeProgramDTO {
    @NotNull
    private Long degreeProgramId;
    @NotNull
    private String name;
    @NotNull
    private ResponsibleUserDTO responsibleUser;

    private List<DegreeProgramSpecializationDTO> degreeProgramSpecializations;

    private ExaminationBoardSummaryDTO examinationBoard;

    public static DegreeProgramDTO fromDegreeProgram(DegreeProgram program) {
        DegreeProgramDTO dto = new DegreeProgramDTO();
        dto.setDegreeProgramId(program.getDegreeProgramId());
        dto.setName(program.getName());
        if (program.getResponsibleUser() != null) {
            dto.setResponsibleUser(ResponsibleUserDTO.fromUser(program.getResponsibleUser()));
        }
        dto.setExaminationBoard(ExaminationBoardSummaryDTO.fromEntity(program.getExaminationBoard()));
        if (program.getDegreeProgramSpecializations() != null) {
            dto.setDegreeProgramSpecializations(program.getDegreeProgramSpecializations().stream()
                    .map(DegreeProgramSpecializationDTO::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }
}
