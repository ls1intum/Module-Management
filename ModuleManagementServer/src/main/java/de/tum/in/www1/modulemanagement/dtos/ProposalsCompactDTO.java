package de.tum.in.www1.modulemanagement.dtos;

import de.tum.in.www1.modulemanagement.enums.ProposalStatus;
import lombok.Data;

@Data
public class ProposalsCompactDTO {
    private Long proposalId;
    private String createdBy;
    private ProposalStatus status;
    private Long latestVersion;
    private String latestTitle;

    public ProposalsCompactDTO(Long proposalId, String createdBy, ProposalStatus status, Long latestVersion, String latestTitle) {
        this.proposalId = proposalId;
        this.createdBy = createdBy;
        this.status = status;
        this.latestVersion = latestVersion;
        this.latestTitle = latestTitle;
    }
}
