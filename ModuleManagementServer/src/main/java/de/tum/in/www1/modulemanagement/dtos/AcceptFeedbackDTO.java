package de.tum.in.www1.modulemanagement.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AcceptFeedbackDTO {
    @NotNull private Long userId;
}
