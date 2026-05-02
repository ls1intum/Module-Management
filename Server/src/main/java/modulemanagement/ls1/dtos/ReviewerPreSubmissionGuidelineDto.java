package modulemanagement.ls1.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.ReviewerPreSubmissionGuideline;
import modulemanagement.ls1.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerPreSubmissionGuidelineDto {
    private Long guidelineId;
    private UserRole reviewerRole;
    private String title;
    private String content;
    private String goodExample;
    private String badExample;
    private String relatedModuleFieldKey;
    private int sortOrder;
    private boolean active;
    private UUID authorUserId;
    private String authorFirstName;
    private String authorLastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewerPreSubmissionGuidelineDto from(ReviewerPreSubmissionGuideline g) {
        User a = g.getAuthor();
        return new ReviewerPreSubmissionGuidelineDto(
                g.getGuidelineId(),
                g.getReviewerRole(),
                g.getTitle(),
                g.getContent(),
                g.getGoodExample(),
                g.getBadExample(),
                g.getRelatedModuleFieldKey(),
                g.getSortOrder(),
                g.isActive(),
                a != null ? a.getUserId() : null,
                a != null ? a.getFirstName() : null,
                a != null ? a.getLastName() : null,
                g.getCreatedAt(),
                g.getUpdatedAt());
    }
}
