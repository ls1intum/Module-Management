package modulemanagement.ls1.modulemanagementserver.controllers;

import modulemanagement.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.dtos.RejectFeedbackDTO;
import modulemanagement.dtos.UserIdDTO;
import modulemanagement.models.Feedback;
import modulemanagement.services.FeedbackService;
import modulemanagement.services.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/for-user/{id}")
    public ResponseEntity<List<Feedback>> getFeedbacksForUser(@PathVariable Long id) {
        List<Feedback> feedbacks = feedbackService.getAllFeedbacksForUser(id);
        return ResponseEntity.ok(feedbacks);
    }

    @PutMapping("/{feedbackId}/accept")
    public ResponseEntity<Feedback> approveFeedback(@PathVariable Long feedbackId, @Valid @RequestBody UserIdDTO request) {
        Feedback updatedFeedback = feedbackService.Accept(feedbackId, request.getUserId());
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());

        return ResponseEntity.ok(updatedFeedback);
    }

    @GetMapping("/module-version-of-feedback/{feedbackId}")
    public ResponseEntity<ModuleVersionUpdateRequestDTO> getModuleVersionOfFeedback(@PathVariable Long feedbackId) {
        ModuleVersionUpdateRequestDTO dto = feedbackService.getModuleVersionOfFeedback(feedbackId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{feedbackId}/reject")
    public ResponseEntity<Feedback> rejectFeedback(@PathVariable Long feedbackId, @Valid @RequestBody RejectFeedbackDTO request) {
        Feedback updatedFeedback = feedbackService.Reject(feedbackId, request.getUserId(), request.getComment());
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());

        return ResponseEntity.ok(updatedFeedback);
    }
}
