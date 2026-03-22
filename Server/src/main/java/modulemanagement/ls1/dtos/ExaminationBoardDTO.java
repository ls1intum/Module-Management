package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.models.ExaminationBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExaminationBoardDTO {
    private Long examinationBoardId;
    private String name;
    private List<ResponsibleUserDTO> members = new ArrayList<>();

    public static ExaminationBoardDTO fromEntity(ExaminationBoard board) {
        ExaminationBoardDTO dto = new ExaminationBoardDTO();
        dto.setExaminationBoardId(board.getExaminationBoardId());
        dto.setName(board.getName());
        if (board.getMembers() != null) {
            dto.setMembers(board.getMembers().stream().map(ResponsibleUserDTO::fromUser)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
