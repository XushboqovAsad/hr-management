package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LmsCourseRepository extends JpaRepository<LmsCourse, UUID> {
    Optional<LmsCourse> findByIdAndDeletedFalse(UUID id);
    Optional<LmsCourse> findByCodeIgnoreCaseAndDeletedFalse(String code);
    List<LmsCourse> findAllByDeletedFalseOrderByTitleAsc();
    List<LmsCourse> findAllByDeletedFalseAndStatusOrderByTitleAsc(LmsCourseStatus status);

    @Query(
            "select c from LmsCourse c where c.deleted = false and (:query is null or lower(c.title) like lower(concat('%', :query, '%')) " +
                    "or lower(c.code) like lower(concat('%', :query, '%')) or lower(coalesce(c.category, '')) like lower(concat('%', :query, '%'))) " +
                    "and (:status is null or c.status = :status) order by c.title asc"
    )
    List<LmsCourse> search(@Param("query") String query, @Param("status") LmsCourseStatus status);
}
