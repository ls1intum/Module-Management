package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.ReviewerPreSubmissionGuideline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewerPreSubmissionGuidelineRepository extends JpaRepository<ReviewerPreSubmissionGuideline, Long> {

    List<ReviewerPreSubmissionGuideline> findByAuthor_UserId(UUID authorUserId);
}
