package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, UUID> {
    List<AttendanceAdjustment> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);
}
