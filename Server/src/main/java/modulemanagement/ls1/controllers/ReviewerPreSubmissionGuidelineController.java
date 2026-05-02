package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.ReviewerPreSubmissionGuidelineDto;
import modulemanagement.ls1.dtos.ReviewerPreSubmissionGuidelineWriteDto;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.ReviewerPreSubmissionGuidelineService;
import modulemanagement.ls1.shared.CurrentUser;
import modulemanagement.ls1.shared.ReviewerRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviewer-pre-submission-guidelines")
public class ReviewerPreSubmissionGuidelineController {

    private final ReviewerPreSubmissionGuidelineService guidelineService;

    public ReviewerPreSubmissionGuidelineController(ReviewerPreSubmissionGuidelineService guidelineService) {
        this.guidelineService = guidelineService;
    }

    @GetMapping
    @PreAuthorize(ReviewerRoles.HAS_ANY_REVIEWER_ROLE)
    public ResponseEntity<List<ReviewerPreSubmissionGuidelineDto>> list(@CurrentUser User user) {
        return ResponseEntity.ok(guidelineService.listForUser(user));
    }

    @PostMapping
    @PreAuthorize(ReviewerRoles.HAS_ANY_REVIEWER_ROLE)
    public ResponseEntity<ReviewerPreSubmissionGuidelineDto> create(@CurrentUser User user,
            @Valid @RequestBody ReviewerPreSubmissionGuidelineWriteDto body) {
        return ResponseEntity.ok(guidelineService.create(user, body));
    }

    @PutMapping("/{guidelineId}")
    @PreAuthorize(ReviewerRoles.HAS_ANY_REVIEWER_ROLE)
    public ResponseEntity<ReviewerPreSubmissionGuidelineDto> update(@CurrentUser User user,
            @PathVariable long guidelineId,
            @Valid @RequestBody ReviewerPreSubmissionGuidelineWriteDto body) {
        return ResponseEntity.ok(guidelineService.update(user, guidelineId, body));
    }

    @DeleteMapping("/{guidelineId}")
    @PreAuthorize(ReviewerRoles.HAS_ANY_REVIEWER_ROLE)
    public ResponseEntity<Void> delete(@CurrentUser User user, @PathVariable long guidelineId) {
        guidelineService.delete(user, guidelineId);
        return ResponseEntity.noContent().build();
    }
}
