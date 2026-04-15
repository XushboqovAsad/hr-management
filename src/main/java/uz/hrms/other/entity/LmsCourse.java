package uz.hrms.other.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "hr", name = "lms_courses")
public class LmsCourse extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_level", nullable = false, length = 30)
    private LmsCourseLevel courseLevel = LmsCourseLevel.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LmsCourseStatus status = LmsCourseStatus.DRAFT;

    @Column(name = "mandatory_for_all", nullable = false)
    private boolean mandatoryForAll;

    @Column(name = "introductory_course", nullable = false)
    private boolean introductoryCourse;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes = 0;

    @Column(name = "certificate_enabled", nullable = false)
    private boolean certificateEnabled = true;

    @Column(name = "certificate_template_code", length = 100)
    private String certificateTemplateCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LmsCourseLevel getCourseLevel() { return courseLevel; }
    public void setCourseLevel(LmsCourseLevel courseLevel) { this.courseLevel = courseLevel; }
    public LmsCourseStatus getStatus() { return status; }
    public void setStatus(LmsCourseStatus status) { this.status = status; }
    public boolean isMandatoryForAll() { return mandatoryForAll; }
    public void setMandatoryForAll(boolean mandatoryForAll) { this.mandatoryForAll = mandatoryForAll; }
    public boolean isIntroductoryCourse() { return introductoryCourse; }
    public void setIntroductoryCourse(boolean introductoryCourse) { this.introductoryCourse = introductoryCourse; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public boolean isCertificateEnabled() { return certificateEnabled; }
    public void setCertificateEnabled(boolean certificateEnabled) { this.certificateEnabled = certificateEnabled; }
    public String getCertificateTemplateCode() { return certificateTemplateCode; }
    public void setCertificateTemplateCode(String certificateTemplateCode) { this.certificateTemplateCode = certificateTemplateCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
