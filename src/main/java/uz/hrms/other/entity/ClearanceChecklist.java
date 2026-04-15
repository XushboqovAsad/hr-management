package uz.hrms.other.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import uz.hrms.other.enums.ClearanceChecklistStatus;

@Entity
@Table(schema = "hr", name = "clearance_checklists")
public class ClearanceChecklist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissal_request_id", nullable = false)
    private DismissalRequest dismissalRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "checklist_status", nullable = false, length = 30)
    private ClearanceChecklistStatus checklistStatus = ClearanceChecklistStatus.OPEN;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public DismissalRequest getDismissalRequest() { return dismissalRequest; }
    public void setDismissalRequest(DismissalRequest dismissalRequest) { this.dismissalRequest = dismissalRequest; }
    public ClearanceChecklistStatus getChecklistStatus() { return checklistStatus; }
    public void setChecklistStatus(ClearanceChecklistStatus checklistStatus) { this.checklistStatus = checklistStatus; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}

