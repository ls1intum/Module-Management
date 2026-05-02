package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateExaminationBoardDTO {

    @NotBlank
    @Size(max = 512)
    private String name;

    @NotNull
    private List<UUID> userIds = new ArrayList<>();
}
