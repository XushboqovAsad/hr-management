package uz.hrms.other.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.EmployeeAssignment;

public interface DismissalEmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    List<EmployeeAssignment> findAllByEmployeeIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(UUID employeeId);
}

