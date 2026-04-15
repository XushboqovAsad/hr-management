package uz.hrms.other.dto.employeeDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeSelfNotificationResponse(
        UUID id,
        String notificationType,
        String title,
        String body,
        String entityType,
        UUID entityId,
        NotificationStatus status,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}
