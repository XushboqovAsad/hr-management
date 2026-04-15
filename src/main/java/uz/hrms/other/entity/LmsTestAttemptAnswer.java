package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "hr", name = "lms_test_attempt_answers")
public class LmsTestAttemptAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private LmsTestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LmsTestQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private LmsTestOption selectedOption;

    @Column(name = "free_text_answer", length = 2000)
    private String freeTextAnswer;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "points_awarded", precision = 8, scale = 2)
    private BigDecimal pointsAwarded;

    public LmsTestAttempt getAttempt() { return attempt; }
    public void setAttempt(LmsTestAttempt attempt) { this.attempt = attempt; }
    public LmsTestQuestion getQuestion() { return question; }
    public void setQuestion(LmsTestQuestion question) { this.question = question; }
    public LmsTestOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(LmsTestOption selectedOption) { this.selectedOption = selectedOption; }
    public String getFreeTextAnswer() { return freeTextAnswer; }
    public void setFreeTextAnswer(String freeTextAnswer) { this.freeTextAnswer = freeTextAnswer; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public BigDecimal getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(BigDecimal pointsAwarded) { this.pointsAwarded = pointsAwarded; }
}
