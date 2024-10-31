package de.tum.in.www1.modulemanagement.repositories;

import de.tum.in.www1.modulemanagement.models.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
}

