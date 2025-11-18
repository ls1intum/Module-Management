package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.FeedbackDTO;
import modulemanagement.ls1.dtos.FeedbackListItemDto;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.GiveFeedbackDTO;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.FeedbackService;
import modulemanagement.ls1.services.ModuleVersionService;
import modulemanagement.ls1.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final ModuleVersionService moduleVersionService;

    public FeedbackController(FeedbackService feedbackService, ModuleVersionService moduleVersionService) {
        this.feedbackService = feedbackService;
        this.moduleVersionService = moduleVersionService;
    }

    @GetMapping("/for-authenticated-user")
    @PreAuthorize("hasAnyRole('QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
    public ResponseEntity<List<FeedbackListItemDto>> getFeedbacksForAuthenticatedUser(@CurrentUser User user) {
        List<FeedbackListItemDto> feedbacks = feedbackService.getAllFeedbacksForUser(user);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/module-version-of-feedback/{feedbackId}")
    @PreAuthorize("hasAnyRole('QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
    public ResponseEntity<ModuleVersionUpdateRequestDTO> getModuleVersionOfFeedback(@PathVariable Long feedbackId) {
        ModuleVersionUpdateRequestDTO dto = feedbackService.getModuleVersionOfFeedback(feedbackId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{feedbackId}/accept")
    @PreAuthorize("hasAnyRole('QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
    public ResponseEntity<Feedback> approveFeedback(@CurrentUser User user, @PathVariable Long feedbackId) {
        Feedback updatedFeedback = feedbackService.Accept(feedbackId, user);
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());
        return ResponseEntity.ok(updatedFeedback);
    }

    @PutMapping("/{feedbackId}/give-feedback")
    @PreAuthorize("hasAnyRole('QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
    public ResponseEntity<Feedback> giveFeedback(@CurrentUser User user, @PathVariable Long feedbackId,
            @Valid @RequestBody FeedbackDTO givenFeedback) {
        Feedback updatedFeedback = feedbackService.GiveFeedback(feedbackId, user, givenFeedback);
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());
        return ResponseEntity.ok(updatedFeedback);
    }

    @PutMapping("/{feedbackId}/reject")
    @PreAuthorize("hasAnyRole('QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
    public ResponseEntity<Feedback> rejectFeedback(@CurrentUser User user, @PathVariable Long feedbackId,
            @Valid @RequestBody GiveFeedbackDTO request) {
        Feedback updatedFeedback = feedbackService.RejectFeedback(feedbackId, user, request.getComment());
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());

        return ResponseEntity.ok(updatedFeedback);
    }

    @GetMapping(value = "/{moduleVersionId}/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
    public ResponseEntity<Resource> exportModuleVersionPdf(@CurrentUser User user,
            @PathVariable Long moduleVersionId) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        String.format("inline; filename=module_version_%s.pdf", moduleVersionId))
                .body(moduleVersionService.generateReviewerModuleVersionPdf(moduleVersionId, user));
    }
}
