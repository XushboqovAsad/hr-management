package uz.hrms.other.dto.employeeDtos;

public record EmployeeSelfFeatureAvailabilityResponse(
        boolean documentsAvailable,
        boolean ordersAcknowledgementAvailable,
        boolean leaveAvailable,
        boolean leaveBalanceAvailable,
        boolean payrollBasisAvailable,
        boolean payslipsAvailable,
        boolean learningAvailable,
        boolean libraryAvailable,
        boolean directoryAvailable,
        boolean notificationsAvailable,
        boolean attendanceAvailable,
        boolean absencesAvailable,
        boolean businessTripsAvailable,
        boolean explanationsAvailable,
        boolean dismissalsAvailable
) {
}
