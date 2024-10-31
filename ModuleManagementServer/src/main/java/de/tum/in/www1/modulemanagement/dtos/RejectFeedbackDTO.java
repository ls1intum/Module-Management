package de.tum.in.www1.modulemanagement.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectFeedbackDTO {
    @NotNull
    private Long userId;
    @NotBlank
    private String comment;
}
