package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uz.hrms.other.enums.BusinessTripDocumentKind;

@Entity
@Table(schema = "hr", name = "business_trip_documents")
public class BusinessTripDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_trip_id", nullable = false)
    private BusinessTrip businessTrip;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_kind", nullable = false, length = 40)
    private uz.hrms.other.enums.BusinessTripDocumentKind documentKind;

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

    @Column(name = "description", length = 1000)
    private String description;

    public BusinessTrip getBusinessTrip() { return businessTrip; }
    public void setBusinessTrip(BusinessTrip businessTrip) { this.businessTrip = businessTrip; }
    public uz.hrms.other.enums.BusinessTripDocumentKind getDocumentKind() { return documentKind; }
    public void setDocumentKind(BusinessTripDocumentKind documentKind) { this.documentKind = documentKind; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

