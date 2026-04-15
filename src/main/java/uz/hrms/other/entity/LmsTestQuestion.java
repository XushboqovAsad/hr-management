package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "hr", name = "lms_test_questions")
public class LmsTestQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LmsTest test;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private LmsQuestionType questionType;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @Column(name = "points", nullable = false, precision = 8, scale = 2)
    private BigDecimal points = BigDecimal.ONE;

    public LmsTest getTest() { return test; }
    public void setTest(LmsTest test) { this.test = test; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
    public LmsQuestionType getQuestionType() { return questionType; }
    public void setQuestionType(LmsQuestionType questionType) { this.questionType = questionType; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public BigDecimal getPoints() { return points; }
    public void setPoints(BigDecimal points) { this.points = points; }
}
