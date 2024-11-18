package modulemanagement.ls1.modulemanagementserver.repositories;

import de.tum.in.www1.modulemanagement.models.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
}

