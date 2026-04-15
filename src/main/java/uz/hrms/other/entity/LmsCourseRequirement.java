package uz.hrms.other.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "hr", name = "lms_course_requirements")
public class LmsCourseRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private LmsRequirementScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "due_days", nullable = false)
    private Integer dueDays = 30;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public LmsRequirementScopeType getScopeType() { return scopeType; }
    public void setScopeType(LmsRequirementScopeType scopeType) { this.scopeType = scopeType; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Integer getDueDays() { return dueDays; }
    public void setDueDays(Integer dueDays) { this.dueDays = dueDays; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
