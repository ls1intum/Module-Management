package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.FeedbackDTO;
import modulemanagement.ls1.dtos.FeedbackListItemDto;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.GiveFeedbackDTO;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AuthenticationService;
import modulemanagement.ls1.services.FeedbackService;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final ModuleVersionService moduleVersionService;
    private final AuthenticationService authenticationService;

    public FeedbackController(FeedbackService feedbackService, ModuleVersionService moduleVersionService, AuthenticationService authenticationService) {
        this.feedbackService = feedbackService;
        this.moduleVersionService = moduleVersionService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/for-authenticated-user")
    @PreAuthorize("hasAnyRole('admin', 'module-reviewer')")
    public ResponseEntity<List<FeedbackListItemDto>> getFeedbacksForAuthenticatedUser(@AuthenticationPrincipal Jwt jwt) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        List<FeedbackListItemDto> feedbacks = feedbackService.getAllFeedbacksForUser(user);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/module-version-of-feedback/{feedbackId}")
    @PreAuthorize("hasAnyRole('admin', 'module-reviewer')")
    public ResponseEntity<ModuleVersionUpdateRequestDTO> getModuleVersionOfFeedback(@PathVariable Long feedbackId) {
        ModuleVersionUpdateRequestDTO dto = feedbackService.getModuleVersionOfFeedback(feedbackId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{feedbackId}/accept")
    @PreAuthorize("hasAnyRole('admin', 'module-reviewer')")
    public ResponseEntity<Feedback> approveFeedback(@AuthenticationPrincipal Jwt jwt, @PathVariable Long feedbackId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        Feedback updatedFeedback = feedbackService.Accept(feedbackId, user);
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());
        return ResponseEntity.ok(updatedFeedback);
    }

    @PutMapping("/{feedbackId}/give-feedback")
    @PreAuthorize("hasAnyRole('admin', 'module-reviewer')")
    public ResponseEntity<Feedback> giveFeedback(@AuthenticationPrincipal Jwt jwt, @PathVariable Long feedbackId, @Valid @RequestBody FeedbackDTO givenFeedback) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        Feedback updatedFeedback = feedbackService.GiveFeedback(feedbackId, user, givenFeedback);
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());
        return ResponseEntity.ok(updatedFeedback);
    }

    @PutMapping("/{feedbackId}/reject")
    @PreAuthorize("hasAnyRole('admin', 'module-reviewer')")
    public ResponseEntity<Feedback> rejectFeedback(@AuthenticationPrincipal Jwt jwt, @PathVariable Long feedbackId, @Valid @RequestBody GiveFeedbackDTO request) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        Feedback updatedFeedback = feedbackService.RejectFeedback(feedbackId, user, request.getComment());
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());

        return ResponseEntity.ok(updatedFeedback);
    }

    @GetMapping(value = "/{moduleVersionId}/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('admin', 'module-reviewer')")
    public ResponseEntity<Resource> exportModuleVersionPdf(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=module_version_%s.pdf", moduleVersionId))
                .body(moduleVersionService.generateReviewerModuleVersionPdf(moduleVersionId, user));
    }
}
