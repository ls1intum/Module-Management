package de.tum.in.www1.modulemanagement.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectFeedbackDTO {
    @NotNull private Long userId;
    @NotNull private String comment;
}
