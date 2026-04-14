package uz.hrms.employee;

import java.util.UUID;

public final class EmployeeDtos {

    private EmployeeDtos() {
    }

    public record EmployeeProfileResponse(
            UUID employeeId,
            UUID userId,
            String personnelNumber,
            String status,
            String username,
            String fullName,
            UUID departmentId,
            String departmentName,
            UUID managerEmployeeId,
            boolean sensitiveFieldsMasked
    ) {
    }
}
