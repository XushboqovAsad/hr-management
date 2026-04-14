package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.AttendanceMarkSource;
import uz.hrms.other.entity.AttendanceDayMark;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

interface AttendanceDayMarkRepository extends JpaRepository<AttendanceDayMark, UUID> {
    List<AttendanceDayMark> findAllByEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByAttendanceDateAsc(UUID employeeId, LocalDate from, LocalDate to);

    List<AttendanceDayMark> findAllBySourceRecordIdAndMarkSourceAndDeletedFalse(UUID sourceRecordId, AttendanceMarkSource markSource);
}
