package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DismissalEmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    List<EmployeeAssignment> findAllByEmployeeIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(UUID employeeId);
}
