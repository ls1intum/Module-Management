package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.ExaminationBoard;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExaminationBoardRepository extends JpaRepository<ExaminationBoard, Long> {

    @EntityGraph(attributePaths = { "members" })
    @Query("SELECT e FROM ExaminationBoard e WHERE e.examinationBoardId = :id")
    Optional<ExaminationBoard> findByIdWithMembers(@Param("id") Long id);

    @EntityGraph(attributePaths = { "members" })
    @Query("SELECT e FROM ExaminationBoard e")
    List<ExaminationBoard> findAllWithMembers();

    @Query("SELECT COUNT(e) > 0 FROM ExaminationBoard e JOIN e.members m WHERE m.userId = :userId")
    boolean existsByMemberUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = { "members" })
    @Query("SELECT DISTINCT e FROM ExaminationBoard e JOIN e.members m WHERE m.userId = :userId")
    List<ExaminationBoard> findByMemberUserId(@Param("userId") UUID userId);
}
