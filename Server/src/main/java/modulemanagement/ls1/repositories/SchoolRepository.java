package modulemanagement.ls1.repositories;

import modulemanagement.ls1.models.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    @Query("SELECT s FROM School s WHERE s.deletedAt IS NULL")
    Page<School> findAllNonDeleted(Pageable pageable);

    @Query("SELECT s FROM School s WHERE s.deletedAt IS NULL AND LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<School> findBySearch(@Param("search") String search, Pageable pageable);
}
