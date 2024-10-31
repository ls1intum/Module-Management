package de.tum.in.www1.modulemanagement.controllers;

import de.tum.in.www1.modulemanagement.dtos.AcceptFeedbackDTO;
import de.tum.in.www1.modulemanagement.dtos.RejectFeedbackDTO;
import de.tum.in.www1.modulemanagement.models.Feedback;
import de.tum.in.www1.modulemanagement.services.FeedbackService;
import de.tum.in.www1.modulemanagement.services.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<Feedback> approveFeedback(@PathVariable Long feedbackId, @Valid @RequestBody AcceptFeedbackDTO request) {
        Feedback updatedFeedback = feedbackService.Accept(feedbackId, request.getUserId());
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());

        return ResponseEntity.ok(updatedFeedback);
    }

    @PutMapping("/{feedbackId}/reject")
    public ResponseEntity<Feedback> rejectFeedback(@PathVariable Long feedbackId, @Valid @RequestBody RejectFeedbackDTO request) {
        Feedback updatedFeedback = feedbackService.Reject(feedbackId, request.getUserId(), request.getComment());
        moduleVersionService.updateStatus(updatedFeedback.getModuleVersion().getModuleVersionId());

        return ResponseEntity.ok(updatedFeedback);
    }
}
