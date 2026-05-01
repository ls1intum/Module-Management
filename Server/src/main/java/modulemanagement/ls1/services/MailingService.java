package modulemanagement.ls1.services;

import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class MailingService {

    private static final Logger log = LoggerFactory.getLogger(MailingService.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final boolean mailEnabled;
    private final String senderAddress;
    private final String clientHost;

    public MailingService(
            JavaMailSender mailSender,
            UserRepository userRepository,
            @Value("${module-management.mail.enabled}") boolean mailEnabled,
            @Value("${module-management.mail.sender}") String senderAddress,
            @Value("${app.client-host}") String clientHost) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.mailEnabled = mailEnabled;
        this.senderAddress = senderAddress;
        this.clientHost = clientHost;
    }

    public void sendReviewerRequestNotification(List<Feedback> feedbacks, String moduleTitle) {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return;
        }

        for (Feedback feedback : feedbacks) {
            Set<User> recipients = getFeedbackRecipients(feedback);
            if (recipients.isEmpty()) {
                continue;
            }

            String subject = "New review request for module proposal";
            for (User recipient : recipients) {
                if (recipient == null || isBlank(recipient.getEmail())) {
                    continue;
                }
                String body = buildReviewerRequestBody(feedback, moduleTitle, recipient);
                sendToUsers(List.of(recipient), subject, body);
            }
        }
    }

    public void sendProfessorFeedbackReceivedNotification(Feedback feedback) {
        if (feedback == null || feedback.getModuleVersion() == null
                || feedback.getModuleVersion().getProposal() == null) {
            return;
        }

        User proposalOwner = feedback.getModuleVersion().getProposal().getCreatedBy();
        if (proposalOwner == null || isBlank(proposalOwner.getEmail())) {
            return;
        }

        String title = feedback.getModuleVersion().getTitleEng() != null
                ? feedback.getModuleVersion().getTitleEng()
                : "Untitled module";
        String subject = "New feedback submitted for your module proposal";
        String body = "Hello " + safeName(proposalOwner) + ",\n\n"
                + "A reviewer has submitted feedback for your proposal"
                + " \"" + title + "\".\n"
                + "Feedback status: " + feedback.getStatus() + ".\n"
                + "You can review it in the platform:\n"
                + clientHost + "/proposals/" + feedback.getModuleVersion().getProposal().getProposalId() + "\n\n"
                + "Best regards,\nModule Management";

        sendToUsers(List.of(proposalOwner), subject, body);
    }

    private Set<User> getFeedbackRecipients(Feedback feedback) {
        Set<User> recipients = new LinkedHashSet<>();

        if (feedback.getAssignedReviewer() != null) {
            recipients.add(feedback.getAssignedReviewer());
            return recipients;
        }

        UserRole role = feedback.getRequiredRole();
        if (role == null) {
            return recipients;
        }

        recipients.addAll(userRepository.findByRole(role));
        return recipients;
    }

    private String buildReviewerRequestBody(Feedback feedback, String moduleTitle, User recipient) {
        String reviewerScope;
        if (feedback.getExaminationBoard() != null) {
            reviewerScope = "examination board: " + feedback.getExaminationBoard().getName();
        } else if (feedback.getDegreeProgramSpecialization() != null) {
            reviewerScope = "specialization: " + feedback.getDegreeProgramSpecialization().getName();
        } else if (feedback.getAssignedReviewer() != null) {
            reviewerScope = "assigned reviewer";
        } else {
            reviewerScope = "role: " + feedback.getRequiredRole();
        }
        return "Hello " + safeName(recipient) + ",\n\n"
                + "You have received a new review request for module proposal "
                + "\"" + moduleTitle + "\".\n"
                + "Assigned scope: " + reviewerScope + ".\n"
                + "Please review it here:\n"
                + clientHost + "/feedbacks/view/" + feedback.getFeedbackId() + "\n\n"
                + "Best regards,\nModule Management";
    }

    private void sendToUsers(Iterable<User> users, String subject, String body) {
        List<String> emails = new ArrayList<>();
        for (User user : users) {
            if (user != null && !isBlank(user.getEmail())) {
                emails.add(user.getEmail());
            }
        }

        if (emails.isEmpty()) {
            return;
        }

        for (String email : emails.stream().filter(Objects::nonNull).distinct().toList()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(senderAddress);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(body);
                if (mailEnabled) {
                    mailSender.send(message);
                } else {
                    log.debug("Mail disabled (MAIL_ENABLED=false); would send to {} subject '{}'\n{}", email, subject,
                            body);
                }
            } catch (Exception ex) {
                log.error("Failed to send mail to {}: {}", email, ex.getMessage());
            }
        }
    }

    private String safeName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? user.getUserName() : fullName;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
