package de.tum.in.www1.modulemanagement.dtos;

import de.tum.in.www1.modulemanagement.enums.ModuleVersionStatus;
import lombok.Data;

@Data
public class ModuleVersionCompactDTO {
    private Long moduleVersionId;
    private Integer version;
    private ModuleVersionStatus status;
}
