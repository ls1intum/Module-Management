package modulemanagement.ls1.repositories;

import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByRequiredRoleInAndStatusAndInvalidatedFalse(Collection<UserRole> requiredRoles, FeedbackStatus status);

    /** Feedbacks for specializations that this user is currently responsible for (position-based). */
    List<Feedback> findByDegreeProgramSpecialization_ResponsibleUser_UserIdAndStatusAndInvalidatedFalse(UUID userId, FeedbackStatus status);

    /** All feedbacks for a proposal that are not invalidated (for display on view/edit). */
    List<Feedback> findByModuleVersion_Proposal_ProposalIdAndInvalidatedFalse(Long proposalId);
}
