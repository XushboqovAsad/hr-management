package uz.hrms.other.dto.businessTripDtos;

import uz.hrms.other.enums.BusinessTripApprovalRole;
import uz.hrms.other.enums.BusinessTripApprovalStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

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
