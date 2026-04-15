package uz.hrms.other.dto.absenceDtos;

import uz.hrms.other.enums.AbsenceDocumentStatus;

import java.util.UUID;

public record AbsenceDocumentResponse(
        UUID id,
        String title,
        String originalFileName,
        String contentType,
        Long sizeBytes,
        Integer versionNo,
        boolean current,
        AbsenceDocumentStatus documentStatus,
        String description
) {
}
