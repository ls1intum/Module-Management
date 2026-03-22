package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.DegreeProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DegreeProgramRepository extends JpaRepository<DegreeProgram, Long> {

    boolean existsByResponsibleUser_UserId(UUID userId);

    List<DegreeProgram> findByResponsibleUser_UserId(UUID userId);

    @EntityGraph(attributePaths = { "responsibleUser", "examinationBoard" })
    @Query("SELECT p FROM DegreeProgram p")
    List<DegreeProgram> findAllWithResponsibleUser();

    @EntityGraph(attributePaths = { "responsibleUser", "degreeProgramSpecializations",
            "degreeProgramSpecializations.responsibleUser", "examinationBoard" })
    Optional<DegreeProgram> findWithSpecializationsByDegreeProgramId(Long degreeProgramId);

    @EntityGraph(attributePaths = { "degreeProgramSpecializations", "examinationBoard" })
    @Query("SELECT p FROM DegreeProgram p ORDER BY p.name")
    List<DegreeProgram> findAllWithSpecializations();

    @Modifying
    @Query("UPDATE DegreeProgram p SET p.examinationBoard = null WHERE p.examinationBoard.examinationBoardId = :examinationBoardId")
    void clearExaminationBoardIdByExaminationBoardId(@Param("examinationBoardId") Long examinationBoardId);
}
