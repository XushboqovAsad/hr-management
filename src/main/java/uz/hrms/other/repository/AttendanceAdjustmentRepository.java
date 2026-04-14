package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.AttendanceAdjustment;

import java.util.List;
import java.util.UUID;

interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, UUID> {
    List<AttendanceAdjustment> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);
}
