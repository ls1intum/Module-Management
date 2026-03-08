package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.DegreeProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DegreeProgramRepository extends JpaRepository<DegreeProgram, Long> {

    @EntityGraph(attributePaths = { "responsibleUser" })
    @Query("SELECT p FROM DegreeProgram p")
    List<DegreeProgram> findAllWithResponsibleUser();

    @EntityGraph(attributePaths = { "responsibleUser", "degreeProgramSpecializations",
            "degreeProgramSpecializations.responsibleUser" })
    Optional<DegreeProgram> findWithSpecializationsByDegreeProgramId(Long degreeProgramId);

    @EntityGraph(attributePaths = { "degreeProgramSpecializations" })
    @Query("SELECT p FROM DegreeProgram p ORDER BY p.name")
    List<DegreeProgram> findAllWithSpecializations();
}
