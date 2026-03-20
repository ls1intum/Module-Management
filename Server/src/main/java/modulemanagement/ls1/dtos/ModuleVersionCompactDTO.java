package modulemanagement.ls1.dtos;

import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.models.ModuleVersion;
import lombok.Data;

import java.util.List;

@Data
public class ModuleVersionCompactDTO {
    private Long moduleVersionId;
    private Integer version;
    private String titleEng;
    private ModuleVersionStatus status;
    private Boolean isComplete;
    private List<FeedbackCompactDTO> feedbackList;

    public static ModuleVersionCompactDTO fromModuleVersion(ModuleVersion moduleVersion) {
        var dto = new ModuleVersionCompactDTO();
        dto.moduleVersionId = moduleVersion.getModuleVersionId();
        dto.version = moduleVersion.getVersion();
        dto.titleEng = moduleVersion.getTitleEng();
        dto.status = moduleVersion.getStatus();
        dto.isComplete = moduleVersion.isCompleted();
        dto.feedbackList = FeedbackCompactDTO.fromList(moduleVersion.getRequiredFeedbacks());
        return dto;
    }
}
