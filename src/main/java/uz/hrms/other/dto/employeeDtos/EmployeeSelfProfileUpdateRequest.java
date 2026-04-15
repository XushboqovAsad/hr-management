package uz.hrms.other.dto.employeeDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record EmployeeSelfProfileUpdateRequest(
        @Email @Size(max = 255) String email,
        @Size(max = 100) String middleName
) {
}
