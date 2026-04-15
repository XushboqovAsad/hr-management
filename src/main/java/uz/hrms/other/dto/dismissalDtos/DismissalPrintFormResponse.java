package uz.hrms.other.dto.dismissalDtos;

import java.util.UUID;

record DismissalPrintFormResponse(
        UUID dismissalRequestId,
        String orderNumber,
        String orderTemplateCode,
        String html
) {
}
