package modulemanagement.ls1.services;

import jakarta.validation.constraints.NotBlank;
import modulemanagement.ls1.dtos.FeedbackDTO;
import modulemanagement.ls1.dtos.FeedbackListItemDto;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback Accept(Long feedbackId, User user) {
        Feedback feedback = getPendingFeedback(feedbackId);
        if (user.getRoles() == null || !user.getRoles().contains(feedback.getRequiredRole()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You do not have permission to accept this feedback");
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.APPROVED);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    public Feedback GiveFeedback(Long feedbackId, User user, FeedbackDTO givenFeedback) {
        Feedback feedback = getPendingFeedback(feedbackId);
        if (user.getRoles() == null || !user.getRoles().contains(feedback.getRequiredRole()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You do not have permission to accept this feedback");
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.insert(givenFeedback);
        boolean positive = feedback.isAllFeedbackPositive();
        feedback.setStatus(positive ? FeedbackStatus.APPROVED : FeedbackStatus.FEEDBACK_GIVEN);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    public Feedback RejectFeedback(Long feedbackId, User user, @NotBlank String comment) {
        Feedback feedback = getPendingFeedback(feedbackId);
        if (user.getRoles() == null || !user.getRoles().contains(feedback.getRequiredRole()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You do not have permission to accept this feedback");
        feedback.setFeedbackFrom(user);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.REJECTED);
        feedback.setComment(comment);
        feedback = feedbackRepository.save(feedback);
        return feedback;
    }

    public List<FeedbackListItemDto> getAllFeedbacksForUser(User user) {
        return feedbackRepository.findByRequiredRoleInAndStatus(user.getRoles(), FeedbackStatus.PENDING_FEEDBACK)
                .stream()
                .sorted(Comparator.comparing(Feedback::getFeedbackId))
                .map(FeedbackListItemDto::fromFeedback)
                .toList();
    }

    private Feedback getPendingFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        if (feedback.getStatus() != FeedbackStatus.PENDING_FEEDBACK) {
            throw new IllegalStateException("This module is not " + FeedbackStatus.PENDING_FEEDBACK);
        }
        return feedback;
    }

    public ModuleVersionUpdateRequestDTO getModuleVersionOfFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        return ModuleVersionUpdateRequestDTO.fromModuleVersion(feedback.getModuleVersion());
    }

}
