package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "hr", name = "lms_tests")
public class LmsTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LmsLesson lesson;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "pass_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal passScore;

    @Column(name = "attempt_limit", nullable = false)
    private Integer attemptLimit = 3;

    @Column(name = "randomize_questions", nullable = false)
    private boolean randomizeQuestions;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public LmsLesson getLesson() { return lesson; }
    public void setLesson(LmsLesson lesson) { this.lesson = lesson; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getPassScore() { return passScore; }
    public void setPassScore(BigDecimal passScore) { this.passScore = passScore; }
    public Integer getAttemptLimit() { return attemptLimit; }
    public void setAttemptLimit(Integer attemptLimit) { this.attemptLimit = attemptLimit; }
    public boolean isRandomizeQuestions() { return randomizeQuestions; }
    public void setRandomizeQuestions(boolean randomizeQuestions) { this.randomizeQuestions = randomizeQuestions; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
