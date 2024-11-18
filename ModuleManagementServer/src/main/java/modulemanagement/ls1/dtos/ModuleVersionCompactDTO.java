package modulemanagement.ls1.modulemanagementserver.dtos;

import modulemanagement.enums.ModuleVersionStatus;
import modulemanagement.models.Feedback;
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
