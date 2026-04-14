package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uz.hrms.other.enums.BusinessTripStatus;
import uz.hrms.other.enums.PayrollSyncStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "hr", name = "business_trips")
public class BusinessTrip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_employee_id")
    private Employee requesterEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_department_id")
    private Department approverDepartment;

    @Column(name = "destination_country", length = 100)
    private String destinationCountry;

    @Column(name = "destination_city", nullable = false, length = 150)
    private String destinationCity;

    @Column(name = "destination_address", length = 255)
    private String destinationAddress;

    @Column(name = "purpose", nullable = false, length = 1000)
    private String purpose;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "transport_type", length = 100)
    private String transportType;

    @Column(name = "accommodation_details", length = 1000)
    private String accommodationDetails;

    @Column(name = "daily_allowance", nullable = false, precision = 18, scale = 2)
    private BigDecimal dailyAllowance = BigDecimal.ZERO;

    @Column(name = "funding_source", length = 255)
    private String fundingSource;

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private uz.hrms.other.enums.BusinessTripStatus status = uz.hrms.other.enums.BusinessTripStatus.DRAFT;

    @Column(name = "order_number", length = 100)
    private String orderNumber;

    @Column(name = "order_template_code", length = 100)
    private String orderTemplateCode;

    @Column(name = "order_generated_at")
    private OffsetDateTime orderGeneratedAt;

    @Column(name = "order_print_form_html")
    private String orderPrintFormHtml;

    @Column(name = "order_pdf_document_id")
    private UUID orderPdfDocumentId;

    @Column(name = "report_text")
    private String reportText;

    @Column(name = "report_submitted_at")
    private OffsetDateTime reportSubmittedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_sync_status", nullable = false, length = 30)
    private PayrollSyncStatus payrollSyncStatus = PayrollSyncStatus.PENDING;

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Employee getRequesterEmployee() { return requesterEmployee; }
    public void setRequesterEmployee(Employee requesterEmployee) { this.requesterEmployee = requesterEmployee; }
    public Department getApproverDepartment() { return approverDepartment; }
    public void setApproverDepartment(Department approverDepartment) { this.approverDepartment = approverDepartment; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public String getAccommodationDetails() { return accommodationDetails; }
    public void setAccommodationDetails(String accommodationDetails) { this.accommodationDetails = accommodationDetails; }
    public BigDecimal getDailyAllowance() { return dailyAllowance; }
    public void setDailyAllowance(BigDecimal dailyAllowance) { this.dailyAllowance = dailyAllowance; }
    public String getFundingSource() { return fundingSource; }
    public void setFundingSource(String fundingSource) { this.fundingSource = fundingSource; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public uz.hrms.other.enums.BusinessTripStatus getStatus() { return status; }
    public void setStatus(BusinessTripStatus status) { this.status = status; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOrderTemplateCode() { return orderTemplateCode; }
    public void setOrderTemplateCode(String orderTemplateCode) { this.orderTemplateCode = orderTemplateCode; }
    public OffsetDateTime getOrderGeneratedAt() { return orderGeneratedAt; }
    public void setOrderGeneratedAt(OffsetDateTime orderGeneratedAt) { this.orderGeneratedAt = orderGeneratedAt; }
    public String getOrderPrintFormHtml() { return orderPrintFormHtml; }
    public void setOrderPrintFormHtml(String orderPrintFormHtml) { this.orderPrintFormHtml = orderPrintFormHtml; }
    public UUID getOrderPdfDocumentId() { return orderPdfDocumentId; }
    public void setOrderPdfDocumentId(UUID orderPdfDocumentId) { this.orderPdfDocumentId = orderPdfDocumentId; }
    public String getReportText() { return reportText; }
    public void setReportText(String reportText) { this.reportText = reportText; }
    public OffsetDateTime getReportSubmittedAt() { return reportSubmittedAt; }
    public void setReportSubmittedAt(OffsetDateTime reportSubmittedAt) { this.reportSubmittedAt = reportSubmittedAt; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(OffsetDateTime closedAt) { this.closedAt = closedAt; }
    public PayrollSyncStatus getPayrollSyncStatus() { return payrollSyncStatus; }
    public void setPayrollSyncStatus(PayrollSyncStatus payrollSyncStatus) { this.payrollSyncStatus = payrollSyncStatus; }
}

