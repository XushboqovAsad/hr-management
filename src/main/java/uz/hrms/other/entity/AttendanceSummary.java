package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "hr", name = "attendance_summaries")
public
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
