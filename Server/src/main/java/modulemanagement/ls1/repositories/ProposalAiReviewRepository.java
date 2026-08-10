package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.ProposalAiReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProposalAiReviewRepository extends JpaRepository<ProposalAiReview, Long> {

    Optional<ProposalAiReview> findByModuleVersion_ModuleVersionId(Long moduleVersionId);
}
