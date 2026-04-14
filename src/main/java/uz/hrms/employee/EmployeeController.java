package uz.hrms.employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeQueryService employeeQueryService;

    public EmployeeController(EmployeeQueryService employeeQueryService) {
        this.employeeQueryService = employeeQueryService;
    }

    @GetMapping("/{employeeId}/profile")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #employeeId)")
    @Operation(summary = "Get employee profile with scope-aware authorization")
    public ResponseEntity<EmployeeDtos.EmployeeProfileResponse> getProfile(Authentication authentication,
                                                                           HttpServletRequest request,
                                                                           @PathVariable UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getProfile(authentication, request.getRequestURI(), employeeId));
    }
}
