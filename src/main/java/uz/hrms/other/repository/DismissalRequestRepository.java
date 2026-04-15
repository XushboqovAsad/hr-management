package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DismissalRequestRepository extends JpaRepository<DismissalRequest, UUID> {
    Optional<DismissalRequest> findByIdAndDeletedFalse(UUID id);

    @Query(
            "select dr from DismissalRequest dr " +
                    "join fetch dr.employee e " +
                    "left join fetch dr.department d " +
                    "where dr.deleted = false " +
                    "and (:employeeId is null or e.id = :employeeId) " +
                    "and (:departmentId is null or d.id = :departmentId) " +
                    "and (:status is null or dr.status = :status) " +
                    "order by dr.createdAt desc"
    )
    List<DismissalRequest> search(@Param("employeeId") UUID employeeId, @Param("departmentId") UUID departmentId, @Param("status") DismissalStatus status);
}
