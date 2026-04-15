package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "hr", name = "clearance_checklist_items")
public class ClearanceChecklistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clearance_checklist_id", nullable = false)
    private ClearanceChecklist clearanceChecklist;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 40)
    private ClearanceItemType itemType;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 30)
    private ClearanceItemStatus itemStatus = ClearanceItemStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false, length = 30)
    private ClearanceReturnStatus returnStatus = ClearanceReturnStatus.PENDING;

    @Column(name = "responsible_role", length = 50)
    private String responsibleRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private UserAccount responsibleUser;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "asset_code", length = 100)
    private String assetCode;

    @Column(name = "asset_name", length = 255)
    private String assetName;

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ClearanceChecklist getClearanceChecklist() {
        return clearanceChecklist;
    }

    public void setClearanceChecklist(ClearanceChecklist clearanceChecklist) {
        this.clearanceChecklist = clearanceChecklist;
    }

    public ClearanceItemType getItemType() {
        return itemType;
    }

    public void setItemType(ClearanceItemType itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ClearanceItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ClearanceItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public ClearanceReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(ClearanceReturnStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public UserAccount getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(UserAccount responsibleUser) {
        this.responsibleUser = responsibleUser;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
