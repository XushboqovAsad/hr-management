package uz.hrms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;

public final class AbsenceDomain {
    private AbsenceDomain() {
    }
}

enum AbsenceType {
    SICK_LEAVE,
    EXCUSED_ABSENCE,
    ABSENCE_UNEXCUSED,
    UNPAID_LEAVE,
    REMOTE_WORK,
    DOWNTIME,
    OTHER
}

enum AbsenceStatus {
    DRAFT,
    SUBMITTED,
    HR_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED,
    CLOSED
}

enum AbsenceDocumentStatus {
    ACTIVE,
    ARCHIVED
}

enum AttendanceMarkSource {
    ABSENCE,
    LEAVE,
    BUSINESS_TRIP,
    MANUAL
}

@Entity
@Table(schema = "hr", name = "absence_records")
class AbsenceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_employee_id")
    private Employee requesterEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "absence_type", nullable = false, length = 40)
    private AbsenceType absenceType;

    @Column(name = "reason_text", length = 2000)
    private String reasonText;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "document_required", nullable = false)
    private boolean documentRequired;

    @Column(name = "hr_comment", length = 2000)
    private String hrComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AbsenceStatus status = AbsenceStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_sync_status", nullable = false, length = 30)
    private PayrollSyncStatus payrollSyncStatus = PayrollSyncStatus.PENDING;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getRequesterEmployee() {
        return requesterEmployee;
    }

    public void setRequesterEmployee(Employee requesterEmployee) {
        this.requesterEmployee = requesterEmployee;
    }

    public AbsenceType getAbsenceType() {
        return absenceType;
    }

    public void setAbsenceType(AbsenceType absenceType) {
        this.absenceType = absenceType;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isDocumentRequired() {
        return documentRequired;
    }

    public void setDocumentRequired(boolean documentRequired) {
        this.documentRequired = documentRequired;
    }

    public String getHrComment() {
        return hrComment;
    }

    public void setHrComment(String hrComment) {
        this.hrComment = hrComment;
    }

    public AbsenceStatus getStatus() {
        return status;
    }

    public void setStatus(AbsenceStatus status) {
        this.status = status;
    }

    public PayrollSyncStatus getPayrollSyncStatus() {
        return payrollSyncStatus;
    }

    public void setPayrollSyncStatus(PayrollSyncStatus payrollSyncStatus) {
        this.payrollSyncStatus = payrollSyncStatus;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }
}

@Entity
@Table(schema = "hr", name = "absence_documents")
class AbsenceDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_record_id", nullable = false)
    private AbsenceRecord absenceRecord;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @Column(name = "is_current", nullable = false)
    private boolean current = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 20)
    private AbsenceDocumentStatus documentStatus = AbsenceDocumentStatus.ACTIVE;

    @Column(name = "description", length = 1000)
    private String description;

    public AbsenceRecord getAbsenceRecord() {
        return absenceRecord;
    }

    public void setAbsenceRecord(AbsenceRecord absenceRecord) {
        this.absenceRecord = absenceRecord;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public AbsenceDocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(AbsenceDocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

@Entity
@Table(schema = "hr", name = "absence_history")
class AbsenceHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_record_id", nullable = false)
    private AbsenceRecord absenceRecord;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "status_from", length = 30)
    private String statusFrom;

    @Column(name = "status_to", length = 30)
    private String statusTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccount actorUser;

    @Column(name = "comment_text", length = 1000)
    private String commentText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public AbsenceRecord getAbsenceRecord() {
        return absenceRecord;
    }

    public void setAbsenceRecord(AbsenceRecord absenceRecord) {
        this.absenceRecord = absenceRecord;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getStatusFrom() {
        return statusFrom;
    }

    public void setStatusFrom(String statusFrom) {
        this.statusFrom = statusFrom;
    }

    public String getStatusTo() {
        return statusTo;
    }

    public void setStatusTo(String statusTo) {
        this.statusTo = statusTo;
    }

    public UserAccount getActorUser() {
        return actorUser;
    }

    public void setActorUser(UserAccount actorUser) {
        this.actorUser = actorUser;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}

@Entity
@Table(schema = "hr", name = "attendance_day_marks")
class AttendanceDayMark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "mark_source", nullable = false, length = 30)
    private AttendanceMarkSource markSource;

    @Column(name = "source_record_id", nullable = false)
    private UUID sourceRecordId;

    @Column(name = "mark_status", nullable = false, length = 40)
    private String markStatus;

    @Column(name = "note_text", length = 1000)
    private String noteText;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceMarkSource getMarkSource() {
        return markSource;
    }

    public void setMarkSource(AttendanceMarkSource markSource) {
        this.markSource = markSource;
    }

    public UUID getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(UUID sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    public String getMarkStatus() {
        return markStatus;
    }

    public void setMarkStatus(String markStatus) {
        this.markStatus = markStatus;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}

interface AbsenceRecordRepository extends JpaRepository<AbsenceRecord, UUID> {
    Optional<AbsenceRecord> findByIdAndDeletedFalse(UUID id);

    List<AbsenceRecord> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<AbsenceRecord> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<AbsenceRecord> findAllByEmployeeIdAndDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID employeeId, LocalDate endDate, LocalDate startDate);
}

interface AbsenceDocumentRepository extends JpaRepository<AbsenceDocument, UUID> {
    Optional<AbsenceDocument> findByIdAndDeletedFalse(UUID id);

    List<AbsenceDocument> findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(UUID absenceRecordId);

    List<AbsenceDocument> findAllByAbsenceRecordIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID absenceRecordId, String title);
}

interface AbsenceHistoryRepository extends JpaRepository<AbsenceHistory, UUID> {
    List<AbsenceHistory> findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(UUID absenceRecordId);
}

interface AttendanceDayMarkRepository extends JpaRepository<AttendanceDayMark, UUID> {
    List<AttendanceDayMark> findAllByEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByAttendanceDateAsc(UUID employeeId, LocalDate from, LocalDate to);

    List<AttendanceDayMark> findAllBySourceRecordIdAndMarkSourceAndDeletedFalse(UUID sourceRecordId, AttendanceMarkSource markSource);
}
