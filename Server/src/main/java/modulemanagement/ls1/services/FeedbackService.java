package modulemanagement.ls1.services;

import jakarta.validation.constraints.NotBlank;
import modulemanagement.ls1.dtos.FeedbackDTO;
import modulemanagement.ls1.dtos.FeedbackListItemDto;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.FeedbackRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Validated
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback Accept(Long feedbackId, User user) {
        Feedback feedback = getPendingFeedback(feedbackId);
        if (!canUserRespondToFeedback(feedback, user))
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
        if (!canUserRespondToFeedback(feedback, user))
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
        if (!canUserRespondToFeedback(feedback, user))
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
        List<Feedback> roleBased = user.getRoles() != null && !user.getRoles().isEmpty()
                ? feedbackRepository.findByRequiredRoleInAndStatus(user.getRoles(), FeedbackStatus.PENDING_FEEDBACK)
                : Collections.emptyList();
        List<Feedback> bySpecialization = feedbackRepository
                .findByDegreeProgramSpecialization_ResponsibleUser_UserIdAndStatus(user.getUserId(),
                        FeedbackStatus.PENDING_FEEDBACK);
        return Stream.of(roleBased.stream(), bySpecialization.stream())
                .flatMap(s -> s)
                .distinct()
                .sorted(Comparator.comparing(Feedback::getFeedbackId))
                .map(FeedbackListItemDto::fromFeedback)
                .toList();
    }

    private boolean canUserRespondToFeedback(Feedback feedback, User user) {
        if (user == null)
            return false;
        if (feedback.getDegreeProgramSpecialization() != null
                && feedback.getDegreeProgramSpecialization().getResponsibleUser() != null) {
            return Objects.equals(user.getUserId(),
                    feedback.getDegreeProgramSpecialization().getResponsibleUser().getUserId());
        }
        return user.getRoles() != null && feedback.getRequiredRole() != null
                && user.getRoles().contains(feedback.getRequiredRole());
    }

    private Feedback getPendingFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        if (feedback.getStatus() != FeedbackStatus.PENDING_FEEDBACK) {
            throw new IllegalStateException("This module is not " + FeedbackStatus.PENDING_FEEDBACK);
        }
        return feedback;
    }

    public ModuleVersionViewDTO getModuleVersionOfFeedback(Long feedbackId, User user) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        if (!canUserRespondToFeedback(feedback, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this feedback.");
        }
        return ModuleVersionViewDTO.from(feedback.getModuleVersion());
    }

}
