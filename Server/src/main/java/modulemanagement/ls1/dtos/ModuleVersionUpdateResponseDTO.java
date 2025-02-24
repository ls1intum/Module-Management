package modulemanagement.ls1.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import modulemanagement.ls1.models.ModuleVersion;

@Setter
@Getter
public class ModuleVersionUpdateResponseDTO extends ModuleVersionUpdateRequestDTO {
    @NotNull private Long proposalId;

    @JsonIgnore
    public static ModuleVersionUpdateResponseDTO fromModuleVersion(ModuleVersion mv) {
        ModuleVersionUpdateResponseDTO mdto = new ModuleVersionUpdateResponseDTO();
        ModuleVersionUpdateRequestDTO.ModuleVersionToRequestDTO(mv, mdto);
        mdto.setProposalId(mv.getProposal().getProposalId());
        return mdto;
    }
}
