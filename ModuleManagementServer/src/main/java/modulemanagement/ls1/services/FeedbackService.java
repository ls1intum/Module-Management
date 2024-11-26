package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.FeedbackListItemDto;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public Feedback Accept(Long feedbackId,UUID userId) {
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

    public Feedback Reject(Long feedbackId, UUID userId, String comment) {
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

    public List<FeedbackListItemDto> getAllFeedbacksForUser(UUID userId) {
        User user = getAuthorizedUser(userId);
        return feedbackRepository.findByRequiredRoleAndStatus(user.getRole(), FeedbackStatus.PENDING_FEEDBACK)
                .stream()
                .filter(feedback -> feedback.getRequiredRole().equals(user.getRole()))
                .sorted(Comparator.comparing(Feedback::getFeedbackId))
                .map(FeedbackListItemDto::fromFeedback)
                .toList();
    }

    private User getAuthorizedUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!user.getFirstName().contains("User")) {
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
