package uz.hrms.other.dto.lmsDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

record LmsCertificateResponse(
        UUID id,
        UUID assignmentId,
        UUID employeeId,
        UUID courseId,
        String certificateNumber,
        OffsetDateTime issuedAt,
        String fileName,
        String mimeType,
        LmsCertificateStatus status
) {
}
