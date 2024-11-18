package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.ModuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleVersionRepository extends JpaRepository<ModuleVersion, Long> {
}
