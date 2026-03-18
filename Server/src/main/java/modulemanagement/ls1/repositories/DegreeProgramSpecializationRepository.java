package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.DegreeProgramSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DegreeProgramSpecializationRepository extends JpaRepository<DegreeProgramSpecialization, Long> {

    @EntityGraph(attributePaths = { "responsibleUser" })
    @Query("SELECT s FROM DegreeProgramSpecialization s")
    List<DegreeProgramSpecialization> findAllWithResponsibleUser();

    @EntityGraph(attributePaths = { "responsibleUser" })
    @Query("SELECT s FROM DegreeProgramSpecialization s WHERE s.degreeProgramSpecializationId = :id")
    Optional<DegreeProgramSpecialization> findByIdWithResponsibleUser(@Param("id") Long id);
}
