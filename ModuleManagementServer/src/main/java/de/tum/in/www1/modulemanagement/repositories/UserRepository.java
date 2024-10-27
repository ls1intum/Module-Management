package de.tum.in.www1.modulemanagement.repositories;

import de.tum.in.www1.modulemanagement.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
