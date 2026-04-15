package uz.hrms.other.dto.businessTripDtos;

import uz.hrms.other.enums.BusinessTripDocumentKind;

import java.util.UUID;

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
