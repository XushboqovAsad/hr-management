package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

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
