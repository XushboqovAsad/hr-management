package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "hr", name = "lms_course_assignments")
public class LmsCourseAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_department_id")
    private Department currentDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_position_id")
    private Position currentPosition;

    @Column(name = "assigned_by_user_id")
    private UUID assignedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_source", nullable = false, length = 20)
    private LmsAssignmentSource assignmentSource;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt = OffsetDateTime.now();

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "last_reminder_at")
    private OffsetDateTime lastReminderAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LmsAssignmentStatus status = LmsAssignmentStatus.ASSIGNED;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public Department getCurrentDepartment() { return currentDepartment; }
    public void setCurrentDepartment(Department currentDepartment) { this.currentDepartment = currentDepartment; }
    public Position getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(Position currentPosition) { this.currentPosition = currentPosition; }
    public UUID getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(UUID assignedByUserId) { this.assignedByUserId = assignedByUserId; }
    public LmsAssignmentSource getAssignmentSource() { return assignmentSource; }
    public void setAssignmentSource(LmsAssignmentSource assignmentSource) { this.assignmentSource = assignmentSource; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(OffsetDateTime assignedAt) { this.assignedAt = assignedAt; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getLastReminderAt() { return lastReminderAt; }
    public void setLastReminderAt(OffsetDateTime lastReminderAt) { this.lastReminderAt = lastReminderAt; }
    public LmsAssignmentStatus getStatus() { return status; }
    public void setStatus(LmsAssignmentStatus status) { this.status = status; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
}
