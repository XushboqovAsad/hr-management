package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.AbsenceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AbsenceRecordRepository extends JpaRepository<AbsenceRecord, UUID> {
    Optional<AbsenceRecord> findByIdAndDeletedFalse(UUID id);

    List<AbsenceRecord> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<AbsenceRecord> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<AbsenceRecord> findAllByEmployeeIdAndDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID employeeId, LocalDate endDate, LocalDate startDate);
}
