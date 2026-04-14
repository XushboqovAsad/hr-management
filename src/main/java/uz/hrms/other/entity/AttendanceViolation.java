package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "hr", name = "lateness_violations")
public
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
