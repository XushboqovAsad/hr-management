package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.Employee;

public final class AbsenceDomain {
    private AbsenceDomain() {
    }
}

enum AbsenceType {
    SICK_LEAVE,
    EXCUSED_ABSENCE,
    ABSENCE_UNEXCUSED,
    UNPAID_LEAVE,
    REMOTE_WORK,
    DOWNTIME,
    OTHER
}

enum AbsenceStatus {
    DRAFT,
    SUBMITTED,
    HR_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED,
    CLOSED
}

enum AbsenceDocumentStatus {
    ACTIVE,
    ARCHIVED
}

enum AttendanceMarkSource {
    ABSENCE,
    LEAVE,
    BUSINESS_TRIP,
    MANUAL
}

interface AbsenceRecordRepository extends JpaRepository<AbsenceRecord, UUID> {
    Optional<AbsenceRecord> findByIdAndDeletedFalse(UUID id);

    List<AbsenceRecord> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<AbsenceRecord> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<AbsenceRecord> findAllByEmployeeIdAndDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID employeeId, LocalDate endDate, LocalDate startDate);
}

interface AbsenceDocumentRepository extends JpaRepository<AbsenceDocument, UUID> {
    Optional<AbsenceDocument> findByIdAndDeletedFalse(UUID id);

    List<AbsenceDocument> findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(UUID absenceRecordId);

    List<AbsenceDocument> findAllByAbsenceRecordIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID absenceRecordId, String title);
}

interface AbsenceHistoryRepository extends JpaRepository<AbsenceHistory, UUID> {
    List<AbsenceHistory> findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(UUID absenceRecordId);
}

interface AttendanceDayMarkRepository extends JpaRepository<AttendanceDayMark, UUID> {
    List<AttendanceDayMark> findAllByEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByAttendanceDateAsc(UUID employeeId, LocalDate from, LocalDate to);

    List<AttendanceDayMark> findAllBySourceRecordIdAndMarkSourceAndDeletedFalse(UUID sourceRecordId, AttendanceMarkSource markSource);
}
