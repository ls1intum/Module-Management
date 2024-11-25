package modulemanagement.ls1.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;

@Data
public class FeedbackListItemDto {
    @NotNull private Long feedbackId;
    @NotNull private Long moduleVersionId;
    @NotNull private Long proposalId;
    @NotNull private FeedbackStatus status;
    @NotNull private String proposalCreatedByName;
    @NotNull private Long proposalCreatedById;
    private String moduleVersionTitleEng;

    public static FeedbackListItemDto fromFeedback(Feedback feedback) {
        FeedbackListItemDto dto = new FeedbackListItemDto();
        ModuleVersion mv = feedback.getModuleVersion();
        Proposal p = mv.getProposal();
        User createdBy = p.getCreatedBy();

        dto.setFeedbackId(feedback.getFeedbackId());
        dto.setModuleVersionId(mv.getModuleVersionId());
        dto.setProposalId(p.getProposalId());
        dto.setStatus(feedback.getStatus());
        dto.setModuleVersionTitleEng(mv.getTitleEng());
        dto.setProposalCreatedByName(createdBy.getName());
        dto.setProposalCreatedById(createdBy.getUserId());

        return dto;
    }
}
