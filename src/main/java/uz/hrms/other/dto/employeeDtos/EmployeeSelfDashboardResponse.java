package uz.hrms.other.dto.employeeDtos;

import java.util.List;

public record EmployeeSelfDashboardResponse(
        EmployeeSelfProfileResponse profile,
        AttendanceDashboardResponse attendance,
        List<AbsenceListItemResponse> absences,
        List<BusinessTripListItemResponse> businessTrips,
        List<ExplanationInboxItemResponse> explanations,
        List<DismissalListItemResponse> dismissals,
        List<EmployeeSelfNotificationResponse> notifications,
        long unreadNotificationsCount,
        EmployeeSelfFeatureAvailabilityResponse features
) {
}
