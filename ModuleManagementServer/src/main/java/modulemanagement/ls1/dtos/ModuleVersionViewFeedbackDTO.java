package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;

import java.time.LocalDateTime;

@Data
public class ModuleVersionViewFeedbackDTO {
    @NotNull private Long feedbackId;
    private String feedbackFromFirstName;
    private String feedbackFromLastName;
    private String rejectionComment;
    private UserRole feedbackRole;
    private FeedbackStatus feedbackStatus;
    private LocalDateTime submissionDate;

    public static ModuleVersionViewFeedbackDTO from(Feedback f) {
        var dto = new ModuleVersionViewFeedbackDTO();
        dto.setFeedbackId(f.getFeedbackId());
        if (f.getFeedbackFrom() != null) {
            dto.setFeedbackFromFirstName(f.getFeedbackFrom().getFirstName());
            dto.setFeedbackFromLastName(f.getFeedbackFrom().getLastName());
        }
        dto.setRejectionComment(f.getComment());
        dto.setFeedbackRole(f.getRequiredRole());
        dto.setFeedbackStatus(f.getStatus());
        dto.setSubmissionDate(f.getSubmissionDate());
        return dto;
    }
}
