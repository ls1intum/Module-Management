package de.tum.in.www1.modulemanagement.dtos;

import de.tum.in.www1.modulemanagement.enums.ProposalStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProposalViewDTO {
    private Long proposalId;
    private LocalDateTime creationDate;
    private Integer latestVersion;
    private ProposalStatus status;
    private ModuleVersionCompactDTO latestModuleVersion;
    private List<ModuleVersionCompactDTO> oldModuleVersions;
}
