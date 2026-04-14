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
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;

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
@Table(schema = "hr", name = "work_schedules")
class WorkSchedule extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "shift_start_time", nullable = false)
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time", nullable = false)
    private LocalTime shiftEndTime;

    @Column(name = "crosses_midnight", nullable = false)
    private boolean crossesMidnight;

    @Column(name = "grace_minutes", nullable = false)
    private Integer graceMinutes;

    @Column(name = "required_minutes", nullable = false)
    private Integer requiredMinutes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(LocalTime shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public LocalTime getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(LocalTime shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public boolean isCrossesMidnight() {
        return crossesMidnight;
    }

    public void setCrossesMidnight(boolean crossesMidnight) {
        this.crossesMidnight = crossesMidnight;
    }

    public Integer getGraceMinutes() {
        return graceMinutes;
    }

    public void setGraceMinutes(Integer graceMinutes) {
        this.graceMinutes = graceMinutes;
    }

    public Integer getRequiredMinutes() {
        return requiredMinutes;
    }

    public void setRequiredMinutes(Integer requiredMinutes) {
        this.requiredMinutes = requiredMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
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
@Table(schema = "hr", name = "scud_events")
class ScudEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem;

    @Column(name = "external_event_id", length = 150)
    private String externalEventId;

    @Column(name = "badge_number", length = 100)
    private String badgeNumber;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private ScudEventType eventType;

    @Column(name = "event_at", nullable = false)
    private OffsetDateTime eventAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "normalization_status", nullable = false, length = 20)
    private ScudNormalizationStatus normalizationStatus = ScudNormalizationStatus.NEW;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getExternalEventId() {
        return externalEventId;
    }

    public void setExternalEventId(String externalEventId) {
        this.externalEventId = externalEventId;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public ScudEventType getEventType() {
        return eventType;
    }

    public void setEventType(ScudEventType eventType) {
        this.eventType = eventType;
    }

    public OffsetDateTime getEventAt() {
        return eventAt;
    }

    public void setEventAt(OffsetDateTime eventAt) {
        this.eventAt = eventAt;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public ScudNormalizationStatus getNormalizationStatus() {
        return normalizationStatus;
    }

    public void setNormalizationStatus(ScudNormalizationStatus normalizationStatus) {
        this.normalizationStatus = normalizationStatus;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

@Entity
@Table(schema = "hr", name = "attendance_logs")
class AttendanceLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_schedule_id")
    private WorkSchedule workSchedule;

    @Column(name = "scheduled_start_at")
    private OffsetDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at")
    private OffsetDateTime scheduledEndAt;

    @Column(name = "first_in_at")
    private OffsetDateTime firstInAt;

    @Column(name = "last_out_at")
    private OffsetDateTime lastOutAt;

    @Column(name = "worked_minutes", nullable = false)
    private Integer workedMinutes = 0;

    @Column(name = "raw_event_count", nullable = false)
    private Integer rawEventCount = 0;

    @Column(name = "missing_in", nullable = false)
    private boolean missingIn;

    @Column(name = "missing_out", nullable = false)
    private boolean missingOut;

    @Column(name = "no_scud_data", nullable = false)
    private boolean noScudData;

    @Column(name = "log_status", nullable = false, length = 20)
    private String logStatus;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public OffsetDateTime getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(OffsetDateTime scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public OffsetDateTime getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(OffsetDateTime scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    public OffsetDateTime getFirstInAt() {
        return firstInAt;
    }

    public void setFirstInAt(OffsetDateTime firstInAt) {
        this.firstInAt = firstInAt;
    }

    public OffsetDateTime getLastOutAt() {
        return lastOutAt;
    }

    public void setLastOutAt(OffsetDateTime lastOutAt) {
        this.lastOutAt = lastOutAt;
    }

    public Integer getWorkedMinutes() {
        return workedMinutes;
    }

    public void setWorkedMinutes(Integer workedMinutes) {
        this.workedMinutes = workedMinutes;
    }

    public Integer getRawEventCount() {
        return rawEventCount;
    }

    public void setRawEventCount(Integer rawEventCount) {
        this.rawEventCount = rawEventCount;
    }

    public boolean isMissingIn() {
        return missingIn;
    }

    public void setMissingIn(boolean missingIn) {
        this.missingIn = missingIn;
    }

    public boolean isMissingOut() {
        return missingOut;
    }

    public void setMissingOut(boolean missingOut) {
        this.missingOut = missingOut;
    }

    public boolean isNoScudData() {
        return noScudData;
    }

    public void setNoScudData(boolean noScudData) {
        this.noScudData = noScudData;
    }

    public String getLogStatus() {
        return logStatus;
    }

    public void setLogStatus(String logStatus) {
        this.logStatus = logStatus;
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
class AttendanceAdjustment extends BaseEntity {

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

@Entity
@Table(schema = "hr", name = "attendance_incidents")
public class AttendanceIncident extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_summary_id", nullable = false)
    private AttendanceSummary attendanceSummary;

    @Column(name = "incident_type", nullable = false, length = 40)
    private String incidentType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AttendanceIncidentStatus status = AttendanceIncidentStatus.OPEN;

    @Column(name = "explanation_required", nullable = false)
    private boolean explanationRequired = true;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public AttendanceSummary getAttendanceSummary() {
        return attendanceSummary;
    }

    public void setAttendanceSummary(AttendanceSummary attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AttendanceIncidentStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceIncidentStatus status) {
        this.status = status;
    }

    public boolean isExplanationRequired() {
        return explanationRequired;
    }

    public void setExplanationRequired(boolean explanationRequired) {
        this.explanationRequired = explanationRequired;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
    }
}

@Entity
@Table(schema = "hr", name = "lateness_violations")
class AttendanceViolation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_summary_id", nullable = false)
    private AttendanceSummary attendanceSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_incident_id")
    private AttendanceIncident attendanceIncident;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false, length = 40)
    private AttendanceViolationType violationType;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "actual_at")
    private OffsetDateTime actualAt;

    @Column(name = "minutes_value", nullable = false)
    private Integer minutesValue = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AttendanceViolationStatus status = AttendanceViolationStatus.OPEN;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public AttendanceSummary getAttendanceSummary() {
        return attendanceSummary;
    }

    public void setAttendanceSummary(AttendanceSummary attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    public AttendanceIncident getAttendanceIncident() {
        return attendanceIncident;
    }

    public void setAttendanceIncident(AttendanceIncident attendanceIncident) {
        this.attendanceIncident = attendanceIncident;
    }

    public AttendanceViolationType getViolationType() {
        return violationType;
    }

    public void setViolationType(AttendanceViolationType violationType) {
        this.violationType = violationType;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(OffsetDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public OffsetDateTime getActualAt() {
        return actualAt;
    }

    public void setActualAt(OffsetDateTime actualAt) {
        this.actualAt = actualAt;
    }

    public Integer getMinutesValue() {
        return minutesValue;
    }

    public void setMinutesValue(Integer minutesValue) {
        this.minutesValue = minutesValue;
    }

    public AttendanceViolationStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceViolationStatus status) {
        this.status = status;
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

interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {
    Optional<AttendanceLog> findByEmployeeIdAndWorkDateAndDeletedFalse(UUID employeeId, LocalDate workDate);

    List<AttendanceLog> findAllByDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(LocalDate from, LocalDate to);
}

interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, UUID> {
    Optional<AttendanceSummary> findByIdAndDeletedFalse(UUID id);

    Optional<AttendanceSummary> findByEmployeeIdAndWorkDateAndDeletedFalse(UUID employeeId, LocalDate workDate);

    List<AttendanceSummary> findAllByDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(LocalDate from, LocalDate to);

    List<AttendanceSummary> findAllByEmployeeIdAndDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(UUID employeeId, LocalDate from, LocalDate to);
}

interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, UUID> {
    List<AttendanceAdjustment> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);
}

interface AttendanceIncidentRepository extends JpaRepository<AttendanceIncident, UUID> {
    Optional<AttendanceIncident> findByIdAndDeletedFalse(UUID id);

    Optional<AttendanceIncident> findFirstByAttendanceSummaryIdAndIncidentTypeAndDeletedFalse(UUID attendanceSummaryId, String incidentType);

    List<AttendanceIncident> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);
}

interface AttendanceViolationRepository extends JpaRepository<AttendanceViolation, UUID> {
    Optional<AttendanceViolation> findFirstByAttendanceSummaryIdAndViolationTypeAndDeletedFalse(UUID attendanceSummaryId, AttendanceViolationType violationType);

    List<AttendanceViolation> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);

    List<AttendanceViolation> findAllByDeletedFalseOrderByCreatedAtDesc();
}
