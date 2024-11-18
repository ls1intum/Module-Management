package modulemanagement.ls1.dtos;

import modulemanagement.ls1.enums.ProposalStatus;
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
