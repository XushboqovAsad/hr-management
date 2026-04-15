package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.hrms.other.entity.EmployeeAssignment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    @Query(
            "select case when count(a) > 0 then true else false end from EmployeeAssignment a " +
                    "where a.deleted = false and a.endedAt is null and a.managerEmployee.id = :managerEmployeeId and a.employee.id = :employeeId"
    )
    boolean existsCurrentSubordinate(@Param("managerEmployeeId") UUID managerEmployeeId, @Param("employeeId") UUID employeeId);

    default boolean existsCurrentSubordinate(UUID managerEmployeeId, UUID employeeId, LocalDate ignoredDate) {
        return existsCurrentSubordinate(managerEmployeeId, employeeId);
    }

    @Query(
            "select a from EmployeeAssignment a " +
                    "where a.deleted = false and a.employee.id = :employeeId and a.primaryAssignment = true " +
                    "and a.startedAt <= :targetDate and (a.endedAt is null or a.endedAt >= :targetDate) " +
                    "order by a.startedAt desc"
    )
    Optional<EmployeeAssignment> findCurrentPrimaryAssignment(@Param("employeeId") UUID employeeId, @Param("targetDate") LocalDate targetDate);

    long countByStaffingUnitIdAndDeletedFalseAndEndedAtIsNull(UUID staffingUnitId);

    List<EmployeeAssignment> findAllByDepartmentIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(UUID departmentId);

    List<EmployeeAssignment> findAllByEmployeeIdAndDeletedFalseOrderByStartedAtDesc(UUID employeeId);
}
