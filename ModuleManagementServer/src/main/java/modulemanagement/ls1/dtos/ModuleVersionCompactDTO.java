package modulemanagement.ls1.dtos;

import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.models.Feedback;
import lombok.Data;

import java.util.List;

@Data
public class ModuleVersionCompactDTO {
    private Long moduleVersionId;
    private Integer version;
    private String titleEng;
    private ModuleVersionStatus status;
    private Boolean isComplete;
    private List<Feedback> feedbackList;
}
