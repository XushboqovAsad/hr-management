package uz.hrms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

final class TestRequestFactory {

    private TestRequestFactory() {
    }

    static BusinessTripRequest businessTripRequest(UUID employeeId, UUID approverDepartmentId) {
        return new BusinessTripRequest(
            employeeId,
            null,
            approverDepartmentId,
            "Uzbekistan",
            "Tashkent",
            "Amir Temur street, 1",
            "Customer meeting",
            LocalDate.of(2026, 4, 10),
            LocalDate.of(2026, 4, 12),
            "TRAIN",
            "Hotel",
            new BigDecimal("350000.00"),
            "OPEX",
            "Business trip for negotiations"
        );
    }

    static BusinessTripReportRequest businessTripReportRequest() {
        return new BusinessTripReportRequest("Trip report submitted successfully");
    }

    static AttendanceProcessRequest attendanceProcessRequest(LocalDate workDate, UUID employeeId) {
        return new AttendanceProcessRequest(workDate, employeeId);
    }

    static AttendanceAdjustmentRequest attendanceAdjustmentRequest() {
        return new AttendanceAdjustmentRequest(
            OffsetDateTime.parse("2026-04-02T09:00:00+05:00"),
            OffsetDateTime.parse("2026-04-02T18:00:00+05:00"),
            "PRESENT",
            "HR manual adjustment",
            UUID.fromString("00000000-0000-0000-0000-000000000111")
        );
    }

    static ScudEventIngestRequest scudEventIn() {
        return new ScudEventIngestRequest(
            UUID.fromString("00000000-0000-0000-0000-000000000211"),
            "BADGE-001",
            "SCUD",
            "EXT-IN-001",
            "DEVICE-1",
            "Main gate",
            ScudEventType.IN,
            OffsetDateTime.parse("2026-04-02T08:57:00+05:00"),
            "{}"
        );
    }

    static ExplanationIncidentCreateRequest latenessIncident(UUID employeeId, UUID departmentId, UUID managerEmployeeId) {
        return new ExplanationIncidentCreateRequest(
            employeeId,
            departmentId,
            managerEmployeeId,
            ExplanationIncidentSource.MANUAL,
            "LATENESS",
            "Late arrival",
            "Employee arrived after shift start",
            OffsetDateTime.parse("2026-04-02T09:20:00+05:00"),
            OffsetDateTime.parse("2026-04-03T18:00:00+05:00")
        );
    }

    static ExplanationSubmitRequest explanationSubmitRequest() {
        return new ExplanationSubmitRequest("Traffic jam due to road repair works.");
    }

    static ExplanationManagerReviewRequest explanationManagerReviewRequest(UUID managerEmployeeId) {
        return new ExplanationManagerReviewRequest(managerEmployeeId, "Manager reviewed and forwarded to HR.");
    }

    static ExplanationDecisionRequest explanationDecisionRequest(UUID hrReviewerEmployeeId) {
        return new ExplanationDecisionRequest(hrReviewerEmployeeId, "Accepted for first-time case", null, null);
    }

    static DismissalRequestUpsertRequest dismissalRequest(UUID employeeId, UUID departmentId) {
        return new DismissalRequestUpsertRequest(
            employeeId,
            null,
            departmentId,
            DismissalReasonType.RESIGNATION,
            "Employee resignation by own will",
            LocalDate.of(2026, 5, 1),
            "Two weeks notice"
        );
    }

    static DismissalDecisionRequest dismissalDecisionRequest(String commentText) {
        return new DismissalDecisionRequest(commentText);
    }

    static DismissalChecklistItemUpdateRequest dismissalChecklistItemUpdateRequest() {
        return new DismissalChecklistItemUpdateRequest(
            ClearanceItemStatus.COMPLETED,
            ClearanceReturnStatus.RETURNED,
            null,
            OffsetDateTime.parse("2026-04-05T18:00:00+05:00"),
            "ASSET-001",
            "Returned asset",
            "Completed by automated test"
        );
    }
}
