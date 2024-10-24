package de.tum.in.www1.modulemanagement.repositories;

import de.tum.in.www1.modulemanagement.models.CitModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<CitModule, Long> {
    Optional<CitModule> findByModuleId(String moduleId);
    Optional<CitModule> findById(long id);
}
