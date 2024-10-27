package de.tum.in.www1.modulemanagement.repositories;

import de.tum.in.www1.modulemanagement.models.ModuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleVersionRepository extends JpaRepository<ModuleVersion, Long> {
}
