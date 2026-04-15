package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.AttendanceViolation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceViolationRepository extends JpaRepository<AttendanceViolation, UUID> {
    Optional<AttendanceViolation> findFirstByAttendanceSummaryIdAndViolationTypeAndDeletedFalse(UUID attendanceSummaryId, AttendanceViolationType violationType);

    List<AttendanceViolation> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);

    List<AttendanceViolation> findAllByDeletedFalseOrderByCreatedAtDesc();
}
