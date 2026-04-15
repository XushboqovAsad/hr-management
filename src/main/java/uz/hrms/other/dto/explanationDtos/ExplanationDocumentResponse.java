package uz.hrms.other.dto.explanationDtos;

import java.util.UUID;

record ExplanationDocumentResponse(
        UUID id,
        String title,
        String originalFileName,
        String contentType,
        long sizeBytes,
        int versionNo,
        boolean current,
        String description
) {
}
