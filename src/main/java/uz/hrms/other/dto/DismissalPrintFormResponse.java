package uz.hrms.other.dto;

import java.util.UUID;

public record DismissalPrintFormResponse(
    UUID dismissalRequestId,
    String orderNumber,
    String orderTemplateCode,
    String html
) {
}

