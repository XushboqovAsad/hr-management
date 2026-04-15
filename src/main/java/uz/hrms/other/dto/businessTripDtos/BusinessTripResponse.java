package uz.hrms.other.dto.businessTripDtos;

import uz.hrms.other.enums.PayrollSyncStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BusinessTripResponse(
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
