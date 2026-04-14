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

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.*;

public final class AttendanceDomain {
    private AttendanceDomain() {
    }
}

enum ScudEventType {
    ENTRY,
    EXIT
}

enum ScudNormalizationStatus {
    NEW,
    PROCESSED,
    IGNORED,
    ERROR
}

enum AttendanceStatus {
    PRESENT,
    LATE,
    EARLY_LEAVE,
    MISSING_PUNCH,
    ABSENT,
    NO_DATA,
    OVERTIME,
    REMOTE_WORK,
    SICK_LEAVE,
    BUSINESS_TRIP,
    UNPAID_LEAVE,
    EXCUSED_ABSENCE,
    ABSENCE_UNEXCUSED,
    DOWNTIME,
    OTHER,
    MANUAL
}

enum AttendanceViolationType {
    LATENESS,
    EARLY_LEAVE,
    MISSING_PUNCH,
    ABSENCE,
    NO_DATA,
    OVERTIME
}

enum AttendanceViolationStatus {
    OPEN,
    EXPLANATION_REQUESTED,
    EXPLAINED,
    WAIVED,
    CLOSED
}

enum AttendanceIncidentStatus {
    OPEN,
    PENDING_EXPLANATION,
    UNDER_REVIEW,
    RESOLVED,
    WAIVED
}

@Entity
@Table(schema = "hr", name = "employee_work_schedules")
class EmployeeWorkSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkSchedule workSchedule;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = true;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public WorkSchedule getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(WorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}

@Entity
@Table(schema = "hr", name = "attendance_summaries")
class AttendanceSummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_assignment_id")
    private UUID employeeAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id")
    private AttendanceLog attendanceLog;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_schedule_id")
    private WorkSchedule workSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 40)
    private AttendanceStatus attendanceStatus;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;

    @Column(name = "early_leave_minutes", nullable = false)
    private Integer earlyLeaveMinutes = 0;

    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes = 0;

    @Column(name = "absence_minutes", nullable = false)
    private Integer absenceMinutes = 0;

    @Column(name = "violation_count", nullable = false)
    private Integer violationCount = 0;

    @Column(name = "incident_created", nullable = false)
    private boolean incidentCreated;

    @Column(name = "manually_adjusted", nullable = false)
    private boolean manuallyAdjusted;

    @Column(name = "adjusted_comment", length = 1000)
    private String adjustedComment;

    @Column(name = "finalized_at")
    private OffsetDateTime finalizedAt;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public UUID getEmployeeAssignmentId() {
        return employeeAssignmentId;
    }

    public void setEmployeeAssignmentId(UUID employeeAssignmentId) {
        this.employeeAssignmentId = employeeAssignmentId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public AttendanceLog getAttendanceLog() {
        return attendanceLog;
    }

    public void setAttendanceLog(AttendanceLog attendanceLog) {
        this.attendanceLog = attendanceLog;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public WorkSchedule getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(WorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public Integer getEarlyLeaveMinutes() {
        return earlyLeaveMinutes;
    }

    public void setEarlyLeaveMinutes(Integer earlyLeaveMinutes) {
        this.earlyLeaveMinutes = earlyLeaveMinutes;
    }

    public Integer getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(Integer overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public Integer getAbsenceMinutes() {
        return absenceMinutes;
    }

    public void setAbsenceMinutes(Integer absenceMinutes) {
        this.absenceMinutes = absenceMinutes;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }

    public boolean isIncidentCreated() {
        return incidentCreated;
    }

    public void setIncidentCreated(boolean incidentCreated) {
        this.incidentCreated = incidentCreated;
    }

    public boolean isManuallyAdjusted() {
        return manuallyAdjusted;
    }

    public void setManuallyAdjusted(boolean manuallyAdjusted) {
        this.manuallyAdjusted = manuallyAdjusted;
    }

    public String getAdjustedComment() {
        return adjustedComment;
    }

    public void setAdjustedComment(String adjustedComment) {
        this.adjustedComment = adjustedComment;
    }

    public OffsetDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(OffsetDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }
}

@Entity
@Table(schema = "hr", name = "attendance_adjustments")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_summary_id", nullable = false)
    private AttendanceSummary attendanceSummary;

    @Column(name = "adjusted_start_at")
    private OffsetDateTime adjustedStartAt;

    @Column(name = "adjusted_end_at")
    private OffsetDateTime adjustedEndAt;

    @Column(name = "adjusted_status", nullable = false, length = 40)
    private String adjustedStatus;

    @Column(name = "adjusted_reason", nullable = false, length = 1000)
    private String adjustedReason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    public AttendanceSummary getAttendanceSummary() {
        return attendanceSummary;
    }

    public void setAttendanceSummary(AttendanceSummary attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    public OffsetDateTime getAdjustedStartAt() {
        return adjustedStartAt;
    }

    public void setAdjustedStartAt(OffsetDateTime adjustedStartAt) {
        this.adjustedStartAt = adjustedStartAt;
    }

    public OffsetDateTime getAdjustedEndAt() {
        return adjustedEndAt;
    }

    public void setAdjustedEndAt(OffsetDateTime adjustedEndAt) {
        this.adjustedEndAt = adjustedEndAt;
    }

    public String getAdjustedStatus() {
        return adjustedStatus;
    }

    public void setAdjustedStatus(String adjustedStatus) {
        this.adjustedStatus = adjustedStatus;
    }

    public String getAdjustedReason() {
        return adjustedReason;
    }

    public void setAdjustedReason(String adjustedReason) {
        this.adjustedReason = adjustedReason;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}

interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
    Optional<WorkSchedule> findByIdAndDeletedFalse(UUID id);

    Optional<WorkSchedule> findByCodeAndDeletedFalse(String code);
}

interface EmployeeWorkScheduleRepository extends JpaRepository<EmployeeWorkSchedule, UUID> {
    Optional<EmployeeWorkSchedule> findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(UUID employeeId, LocalDate dateStart, LocalDate dateEnd);

    Optional<EmployeeWorkSchedule> findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(UUID employeeId, LocalDate date);
}

interface ScudEventRepository extends JpaRepository<ScudEvent, UUID> {
    Optional<ScudEvent> findByExternalEventIdAndDeletedFalse(String externalEventId);

    List<ScudEvent> findAllByEmployeeIdAndDeletedFalseAndEventAtBetweenOrderByEventAtAsc(UUID employeeId, OffsetDateTime from, OffsetDateTime to);

    List<ScudEvent> findAllByDeletedFalseAndEventAtBetweenOrderByEventAtAsc(OffsetDateTime from, OffsetDateTime to);
}

interface AttendanceViolationRepository extends JpaRepository<AttendanceViolation, UUID> {
    Optional<AttendanceViolation> findFirstByAttendanceSummaryIdAndViolationTypeAndDeletedFalse(UUID attendanceSummaryId, AttendanceViolationType violationType);

    List<AttendanceViolation> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);

    List<AttendanceViolation> findAllByDeletedFalseOrderByCreatedAtDesc();
}
