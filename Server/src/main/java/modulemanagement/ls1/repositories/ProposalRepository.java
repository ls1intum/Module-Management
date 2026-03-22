package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByCreatedBy_UserId(UUID userId);
}

