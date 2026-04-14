package uz.hrms.other;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uz.hrms.other.enums.BusinessTripApprovalRole;
import uz.hrms.other.enums.BusinessTripApprovalStatus;
import uz.hrms.other.enums.BusinessTripDocumentKind;
import uz.hrms.other.enums.BusinessTripStatus;
import uz.hrms.other.enums.PayrollSyncStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record BusinessTripRequest(
    @NotNull UUID employeeId,
    UUID requesterEmployeeId,
    UUID approverDepartmentId,
    String destinationCountry,
    @NotBlank String destinationCity,
    String destinationAddress,
    @NotBlank String purpose,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    String transportType,
    String accommodationDetails,
    @NotNull @DecimalMin("0.00") BigDecimal dailyAllowance,
    String fundingSource,
    String commentText
) {
}

record BusinessTripReportRequest(
    @NotBlank String reportText
) {
}

record BusinessTripApprovalDecisionRequest(
    String commentText
) {
}

record BusinessTripDocumentUploadRequest(
    @NotNull uz.hrms.other.enums.BusinessTripDocumentKind documentKind,
    @NotBlank String title,
    String description
) {
}

record BusinessTripResponse(
    UUID id,
    UUID employeeId,
    UUID requesterEmployeeId,
    UUID approverDepartmentId,
    String destinationCountry,
    String destinationCity,
    String destinationAddress,
    String purpose,
    LocalDate startDate,
    LocalDate endDate,
    String transportType,
    String accommodationDetails,
    BigDecimal dailyAllowance,
    String fundingSource,
    String commentText,
    uz.hrms.other.enums.BusinessTripStatus status,
    String orderNumber,
    OffsetDateTime orderGeneratedAt,
    OffsetDateTime reportSubmittedAt,
    OffsetDateTime closedAt,
    PayrollSyncStatus payrollSyncStatus,
    List<BusinessTripApprovalResponse> approvals,
    List<BusinessTripDocumentResponse> documents,
    List<BusinessTripHistoryResponse> history,
    String orderPrintFormHtml
) {
}

record BusinessTripListItemResponse(
    UUID id,
    UUID employeeId,
    String destinationCity,
    String purpose,
    LocalDate startDate,
    LocalDate endDate,
    BusinessTripStatus status,
    boolean overdueReport
) {
}

record BusinessTripDocumentResponse(
    UUID id,
    BusinessTripDocumentKind documentKind,
    String title,
    String originalFileName,
    String contentType,
    Long sizeBytes,
    Integer versionNo,
    boolean current,
    String description
) {
}

record BusinessTripApprovalResponse(
    UUID id,
    Integer stepNo,
    BusinessTripApprovalRole approvalRole,
    UUID approverUserId,
    BusinessTripApprovalStatus status,
    String decisionComment,
    OffsetDateTime decidedAt
) {
}

record BusinessTripHistoryResponse(
    UUID id,
    String actionType,
    String statusFrom,
    String statusTo,
    UUID actorUserId,
    String commentText,
    OffsetDateTime createdAt
) {
}

record BusinessTripPrintFormResponse(
    UUID id,
    String orderNumber,
    String templateCode,
    String html
) {
}
