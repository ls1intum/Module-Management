package de.tum.in.www1.modulemanagement.dtos;

import de.tum.in.www1.modulemanagement.enums.ModuleVersionStatus;
import de.tum.in.www1.modulemanagement.models.Feedback;
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
