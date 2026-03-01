package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AddSpecializationsToDegreeProgramDTO {

    @NotNull(message = "degreeProgramSpecializationIds must not be null")
    private List<Long> degreeProgramSpecializationIds;

    public List<Long> getDegreeProgramSpecializationIds() {
        return degreeProgramSpecializationIds;
    }

    public void setDegreeProgramSpecializationIds(List<Long> degreeProgramSpecializationIds) {
        this.degreeProgramSpecializationIds = degreeProgramSpecializationIds;
    }
}
