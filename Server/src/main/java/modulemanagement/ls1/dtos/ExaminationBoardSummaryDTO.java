package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.models.ExaminationBoard;

@Data
public class ExaminationBoardSummaryDTO {
    private Long examinationBoardId;
    private String name;

    public static ExaminationBoardSummaryDTO fromEntity(ExaminationBoard board) {
        if (board == null)
            return null;
        ExaminationBoardSummaryDTO dto = new ExaminationBoardSummaryDTO();
        dto.setExaminationBoardId(board.getExaminationBoardId());
        dto.setName(board.getName());
        return dto;
    }
}
