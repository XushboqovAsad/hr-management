package uz.hrms.other.entity;

import jakarta.persistence.*;
import uz.hrms.other.AttendanceIncident;
import uz.hrms.other.BaseEntity;
import uz.hrms.other.ExplanationIncidentSource;
import uz.hrms.other.ExplanationIncidentStatus;

import java.time.OffsetDateTime;

public @Entity
@Table(schema = "hr", name = "explanation_incidents")
class ExplanationIncident extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_incident_id")
    private AttendanceIncident attendanceIncident;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_source", nullable = false, length = 30)
    private ExplanationIncidentSource incidentSource;

    @Column(name = "incident_type", nullable = false, length = 40)
    private String incidentType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private Employee managerEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExplanationIncidentStatus status = ExplanationIncidentStatus.PENDING_EXPLANATION;

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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public AttendanceIncident getAttendanceIncident() {
        return attendanceIncident;
    }

    public void setAttendanceIncident(AttendanceIncident attendanceIncident) {
        this.attendanceIncident = attendanceIncident;
    }

    public ExplanationIncidentSource getIncidentSource() {
        return incidentSource;
    }

    public void setIncidentSource(ExplanationIncidentSource incidentSource) {
        this.incidentSource = incidentSource;
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

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Employee getManagerEmployee() {
        return managerEmployee;
    }

    public void setManagerEmployee(Employee managerEmployee) {
        this.managerEmployee = managerEmployee;
    }

    public ExplanationIncidentStatus getStatus() {
        return status;
    }

    public void setStatus(ExplanationIncidentStatus status) {
        this.status = status;
    }

    public boolean isExplanationRequired() {
        return explanationRequired;
    }

    public boolean getExplanationRequired() {
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