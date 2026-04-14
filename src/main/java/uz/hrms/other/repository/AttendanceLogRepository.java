package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.AttendanceLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {
    Optional<AttendanceLog> findByEmployeeIdAndWorkDateAndDeletedFalse(UUID employeeId, LocalDate workDate);

    List<AttendanceLog> findAllByDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(LocalDate from, LocalDate to);
}
