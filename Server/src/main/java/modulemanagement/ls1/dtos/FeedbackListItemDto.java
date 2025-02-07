package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;

import java.util.UUID;

@Data
public class FeedbackListItemDto {
    @NotNull private Long feedbackId;
    @NotNull private Long moduleVersionId;
    @NotNull private Long proposalId;
    @NotNull private FeedbackStatus status;
    @NotNull private String proposalCreatedByName;
    @NotNull private UUID proposalCreatedById;
    private String moduleVersionTitleEng;

    public static FeedbackListItemDto fromFeedback(Feedback feedback) {
        FeedbackListItemDto dto = new FeedbackListItemDto();
        ModuleVersion mv = feedback.getModuleVersion();
        Proposal p = mv.getProposal();
        User createdBy = p.getCreatedBy();

        dto.feedbackId = feedback.getFeedbackId();
        dto.moduleVersionId = mv.getModuleVersionId();
        dto.proposalId = p.getProposalId();
        dto.status = feedback.getStatus();
        dto.moduleVersionTitleEng = mv.getTitleEng();
        dto.proposalCreatedByName = createdBy.getFirstName() + " " + createdBy.getLastName();
        dto.proposalCreatedById = createdBy.getUserId();

        return dto;
    }
}
