package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.ExplanationIncident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ExplanationIncidentRepository extends JpaRepository<ExplanationIncident, UUID> {
    Optional<ExplanationIncident> findByIdAndDeletedFalse(UUID id);

    Optional<ExplanationIncident> findByAttendanceIncidentIdAndDeletedFalse(UUID attendanceIncidentId);

    List<ExplanationIncident> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<ExplanationIncident> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<ExplanationIncident> findAllByDepartmentIdAndDeletedFalseOrderByCreatedAtDesc(UUID departmentId);
}
