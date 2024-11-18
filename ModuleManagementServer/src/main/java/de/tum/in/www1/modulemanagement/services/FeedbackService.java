package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.dtos.ModuleVersionUpdateRequestDTO;
import de.tum.in.www1.modulemanagement.enums.FeedbackStatus;
import de.tum.in.www1.modulemanagement.models.Feedback;
import de.tum.in.www1.modulemanagement.models.User;
import de.tum.in.www1.modulemanagement.repositories.FeedbackRepository;
import de.tum.in.www1.modulemanagement.repositories.UserRepository;
import de.tum.in.www1.modulemanagement.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public Feedback Accept(Long feedbackId,Long userId) {
        User user = getAuthorizedUser(userId);
        Feedback feedback = getPendingFeedback(feedbackId);
        if (!user.getRole().equals(feedback.getRequiredRole()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You do not have permission to accept this feedback");
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.APPROVED);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    public Feedback Reject(Long feedbackId, Long userId, String comment) {
        User user = getAuthorizedUser(userId);
        Feedback feedback = getPendingFeedback(feedbackId);
        if (!user.getRole().equals(feedback.getRequiredRole()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You do not have permission to accept this feedback");
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.REJECTED);
        feedback.setComment(comment);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    public List<Feedback> getAllFeedbacksForUser(Long userId) {
        User user = getAuthorizedUser(userId);
        return feedbackRepository.findAll()
                .stream()
                .filter(feedback -> feedback.getRequiredRole().equals(user.getRole()))
                .sorted(Comparator.comparing(Feedback::getFeedbackId))
                .toList();
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

    public ModuleVersionUpdateRequestDTO getModuleVersionOfFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        return feedback.getModuleVersion().toModuleUpdateRequestDTO();
    }
}
