package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmployeeSelfServiceAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    @Query(
            "select a from EmployeeAssignment a " +
                    "left join fetch a.department d " +
                    "left join fetch a.managerEmployee m " +
                    "left join fetch m.user mu " +
                    "where a.deleted = false and a.employee.id = :employeeId " +
                    "and a.startedAt <= :today and (a.endedAt is null or a.endedAt >= :today) " +
                    "order by a.startedAt desc"
    )
    List<EmployeeAssignment> findCurrentAssignments(@Param("employeeId") UUID employeeId, @Param("today") LocalDate today);
}
