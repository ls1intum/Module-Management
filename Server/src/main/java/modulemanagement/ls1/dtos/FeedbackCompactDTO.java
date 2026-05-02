package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class FeedbackCompactDTO {
    private Long feedbackId;
    private String requestedFromUserName;
    private String requestedFromSpecializationName;
    private String examinationBoardName;
    private UserRole requiredRole;
    private FeedbackStatus status;
    private Boolean invalidated;

    public static FeedbackCompactDTO from(Feedback f) {
        var dto = new FeedbackCompactDTO();
        dto.setFeedbackId(f.getFeedbackId());
        dto.setRequiredRole(f.getRequiredRole());
        dto.setStatus(f.getStatus());
        dto.setInvalidated(f.isInvalidated());
        if (f.getAssignedReviewer() != null) {
            var ar = f.getAssignedReviewer();
            dto.setRequestedFromUserName(ar.getFirstName() + " " + ar.getLastName());
        }
        if (f.getDegreeProgramSpecialization() != null) {
            var spec = f.getDegreeProgramSpecialization();
            dto.setRequestedFromSpecializationName(spec.getName());
            if (dto.getRequestedFromUserName() == null && spec.getResponsibleUser() != null) {
                var resp = spec.getResponsibleUser();
                dto.setRequestedFromUserName(resp.getFirstName() + " " + resp.getLastName());
            }
        } else if (f.getExaminationBoard() != null) {
            dto.setExaminationBoardName(f.getExaminationBoard().getName());
        } else if (f.getRequiredRole() != null && f.getFeedbackFrom() != null) {
            var user = f.getFeedbackFrom();
            dto.setRequestedFromUserName(user.getFirstName() + " " + user.getLastName());
        }
        return dto;
    }

    public static List<FeedbackCompactDTO> fromList(List<Feedback> feedbacks) {
        if (feedbacks == null) return List.of();
        return feedbacks.stream().map(FeedbackCompactDTO::from).collect(Collectors.toList());
    }
}
