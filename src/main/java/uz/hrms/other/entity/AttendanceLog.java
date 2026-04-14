package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "hr", name = "attendance_logs")
public
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
