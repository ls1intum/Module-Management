package modulemanagement.ls1.dtos;

import modulemanagement.ls1.enums.ProposalStatus;
import lombok.Data;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProposalViewDTO {
    private Long proposalId;
    private LocalDateTime creationDate;
    private Integer latestVersion;
    private ProposalStatus status;
    private ModuleVersionCompactDTO latestModuleVersion;
    private List<ModuleVersionCompactDTO> oldModuleVersions;

    public static ProposalViewDTO from(Proposal p) {
        var v = new ProposalViewDTO();
        v.proposalId = p.getProposalId();
        v.latestVersion = p.getLatestModuleVersion();
        v.creationDate = p.getCreationDate();
        v.status = p.getStatus();

        var sortedModuleVersions = p.getModuleVersions().stream()
                .sorted(Comparator.comparing(ModuleVersion::getVersion).reversed())
                .toList();

        if (!sortedModuleVersions.isEmpty()) {
            v.latestModuleVersion = ModuleVersionCompactDTO.fromModuleVersion(sortedModuleVersions.getFirst());

            v.oldModuleVersions = sortedModuleVersions.subList(1, sortedModuleVersions.size())
                    .stream()
                    .map(ModuleVersionCompactDTO::fromModuleVersion)
                    .collect(Collectors.toList());
        } else {
            v.latestModuleVersion = null;
            v.oldModuleVersions = Collections.emptyList();
            v.setOldModuleVersions(Collections.emptyList());
        }

        return v;
    }
}
