package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.ModuleVersionDegreeProgramAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleVersionDegreeProgramAssignmentRepository
        extends JpaRepository<ModuleVersionDegreeProgramAssignment, Long> {

    @Modifying
    @Query("DELETE FROM ModuleVersionDegreeProgramAssignment a WHERE a.moduleVersion.moduleVersionId = :moduleVersionId")
    void deleteByModuleVersion_ModuleVersionId(@Param("moduleVersionId") Long moduleVersionId);
}
