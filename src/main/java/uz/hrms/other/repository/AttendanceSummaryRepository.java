package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, UUID> {
    Optional<AttendanceSummary> findByIdAndDeletedFalse(UUID id);

    Optional<AttendanceSummary> findByEmployeeIdAndWorkDateAndDeletedFalse(UUID employeeId, LocalDate workDate);

    List<AttendanceSummary> findAllByDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(LocalDate from, LocalDate to);

    List<AttendanceSummary> findAllByEmployeeIdAndDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(UUID employeeId, LocalDate from, LocalDate to);
}
