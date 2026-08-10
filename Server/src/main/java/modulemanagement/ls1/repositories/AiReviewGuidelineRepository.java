package modulemanagement.ls1.repositories;

import modulemanagement.ls1.enums.ProposalReviewSection;
import modulemanagement.ls1.models.AiReviewGuideline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiReviewGuidelineRepository extends JpaRepository<AiReviewGuideline, Long> {

    List<AiReviewGuideline> findAllByOrderBySectionAscSortOrderAscGuidelineIdAsc();

    List<AiReviewGuideline> findBySectionOrderBySortOrderAscGuidelineIdAsc(ProposalReviewSection section);
}
