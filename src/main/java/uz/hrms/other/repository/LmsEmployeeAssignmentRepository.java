package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LmsEmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    @Query(
            "select a from EmployeeAssignment a left join fetch a.department left join fetch a.position where a.deleted = false and a.employee.id = :employeeId and a.startedAt <= :today and (a.endedAt is null or a.endedAt >= :today) order by a.startedAt desc"
    )
    List<EmployeeAssignment> findCurrentAssignments(@Param("employeeId") UUID employeeId, @Param("today") LocalDate today);

    @Query(
            "select a from EmployeeAssignment a left join fetch a.department left join fetch a.position where a.deleted = false and a.startedAt <= :today and (a.endedAt is null or a.endedAt >= :today) order by a.employee.id asc, a.startedAt desc"
    )
    List<EmployeeAssignment> findAllCurrentAssignments(@Param("today") LocalDate today);
}
