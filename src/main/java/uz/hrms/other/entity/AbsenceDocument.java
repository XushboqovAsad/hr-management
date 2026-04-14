package uz.hrms.other.entity;

import jakarta.persistence.*;
import uz.hrms.other.AbsenceDocumentStatus;
import uz.hrms.other.BaseEntity;

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
