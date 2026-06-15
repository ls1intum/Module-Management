package modulemanagement.ls1.repositories;

import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    /** Pending feedbacks explicitly assigned to this user (coordinators, examination board members, …). */
    List<Feedback> findByAssignedReviewer_UserIdAndStatusAndInvalidatedFalse(UUID userId, FeedbackStatus status);

    /** All feedbacks for a proposal that are not invalidated (for display on view/edit). */
    List<Feedback> findByModuleVersion_Proposal_ProposalIdAndInvalidatedFalse(Long proposalId);

    boolean existsByModuleVersion_Proposal_ProposalIdAndInvalidatedFalseAndAssignedReviewer_UserId(
            Long proposalId, UUID userId);
}
