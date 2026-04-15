package uz.hrms.other.dto.businessTripDtos;

import uz.hrms.other.enums.BusinessTripStatus;

import java.time.LocalDate;
import java.util.UUID;

record BusinessTripListItemResponse(
        UUID id,
        UUID employeeId,
        String destinationCity,
        String purpose,
        LocalDate startDate,
        LocalDate endDate,
        BusinessTripStatus status,
        boolean overdueReport
) {
}
