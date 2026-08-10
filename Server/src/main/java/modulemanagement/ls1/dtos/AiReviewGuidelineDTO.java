package modulemanagement.ls1.dtos;

import lombok.Data;
import modulemanagement.ls1.enums.ProposalReviewSection;
import modulemanagement.ls1.models.AiReviewGuideline;

import java.time.LocalDateTime;

@Data
public class AiReviewGuidelineDTO {
    private Long guidelineId;
    private ProposalReviewSection section;
    private String title;
    private String instruction;
    private int sortOrder;
    private String createdByUserId;
    private String createdByDisplayName;
    private String updatedByUserId;
    private String updatedByDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AiReviewGuidelineDTO fromEntity(AiReviewGuideline entity) {
        if (entity == null) {
            return null;
        }
        AiReviewGuidelineDTO dto = new AiReviewGuidelineDTO();
        dto.setGuidelineId(entity.getGuidelineId());
        dto.setSection(entity.getSection());
        dto.setTitle(entity.getTitle());
        dto.setInstruction(entity.getInstruction());
        dto.setSortOrder(entity.getSortOrder());
        if (entity.getCreatedBy() != null) {
            dto.setCreatedByUserId(entity.getCreatedBy().getUserId().toString());
            dto.setCreatedByDisplayName(formatDisplayName(entity.getCreatedBy()));
        }
        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedByUserId(entity.getUpdatedBy().getUserId().toString());
            dto.setUpdatedByDisplayName(formatDisplayName(entity.getUpdatedBy()));
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private static String formatDisplayName(modulemanagement.ls1.models.User user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String combined = (first + " " + last).trim();
        if (!combined.isEmpty()) {
            return combined;
        }
        return user.getUserName() != null ? user.getUserName() : user.getUserId().toString();
    }
}
