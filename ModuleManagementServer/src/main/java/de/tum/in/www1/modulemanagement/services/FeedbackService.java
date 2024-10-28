package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.enums.FeedbackStatus;
import de.tum.in.www1.modulemanagement.models.Feedback;
import de.tum.in.www1.modulemanagement.models.User;
import de.tum.in.www1.modulemanagement.repositories.FeedbackRepository;
import de.tum.in.www1.modulemanagement.repositories.UserRepository;
import de.tum.in.www1.modulemanagement.shared.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public Feedback Accept(@NotNull Long feedbackId, @NotNull Long userId) {
        User user = getAuthorizedUser(userId);
        Feedback feedback = getPendingFeedback(feedbackId);
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.APPROVED);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    public Feedback Reject(@NotNull Long feedbackId, @NotNull Long userId, @NotNull String comment) {
        User user = getAuthorizedUser(userId);
        Feedback feedback = getPendingFeedback(feedbackId);
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.REJECTED);
        feedback.setComment(comment);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    private User getAuthorizedUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!user.getName().contains("User")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }
        return user;
    }

    private Feedback getPendingFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        if (feedback.getStatus() != FeedbackStatus.PENDING_FEEDBACK) {
            throw new IllegalStateException("This module is not " + FeedbackStatus.PENDING_FEEDBACK);
        }
        return feedback;
    }
}
