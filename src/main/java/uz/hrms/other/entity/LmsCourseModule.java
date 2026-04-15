package uz.hrms.other.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "hr", name = "lms_course_modules")
public class LmsCourseModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "module_order", nullable = false)
    private Integer moduleOrder;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getModuleOrder() { return moduleOrder; }
    public void setModuleOrder(Integer moduleOrder) { this.moduleOrder = moduleOrder; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
