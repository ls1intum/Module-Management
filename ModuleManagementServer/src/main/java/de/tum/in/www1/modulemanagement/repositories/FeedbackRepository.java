package de.tum.in.www1.modulemanagement.repositories;

import de.tum.in.www1.modulemanagement.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
