package uz.hrms.other.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "hr", name = "lms_lessons")
public class LmsLesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_module_id", nullable = false)
    private LmsCourseModule courseModule;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private LmsLessonContentType contentType;

    @Column(name = "content_url", length = 1000)
    private String contentUrl;

    @Column(name = "content_text")
    private String contentText;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 0;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    public LmsCourseModule getCourseModule() { return courseModule; }
    public void setCourseModule(LmsCourseModule courseModule) { this.courseModule = courseModule; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getLessonOrder() { return lessonOrder; }
    public void setLessonOrder(Integer lessonOrder) { this.lessonOrder = lessonOrder; }
    public LmsLessonContentType getContentType() { return contentType; }
    public void setContentType(LmsLessonContentType contentType) { this.contentType = contentType; }
    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
