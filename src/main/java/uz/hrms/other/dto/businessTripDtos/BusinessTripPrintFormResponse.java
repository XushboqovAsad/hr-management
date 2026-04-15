package uz.hrms.other.dto.businessTripDtos;

import java.util.UUID;

record BusinessTripPrintFormResponse(
        UUID id,
        String orderNumber,
        String templateCode,
        String html
) {
}
