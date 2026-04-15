package uz.hrms.other.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.AccessPolicy;
import uz.hrms.other.OrganizationService;

@RestController
@RequestMapping("/api/v1/org")
@Validated
class OrganizationController {

    private final OrganizationService organizationService;
    private final AccessPolicy accessPolicy;

    OrganizationController(OrganizationService organizationService, AccessPolicy accessPolicy) {
        this.organizationService = organizationService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping("/departments")
    List<DepartmentResponse> departments(Authentication authentication) {
        ensureDepartmentRead(authentication);
        return organizationService.getDepartments();
    }

    @GetMapping("/departments/tree")
    List<DepartmentTreeNode> departmentTree(Authentication authentication) {
        ensureDepartmentRead(authentication);
        return organizationService.getDepartmentTree();
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    DepartmentResponse createDepartment(Authentication authentication, @Valid @RequestBody DepartmentRequest request) {
        ensureDepartmentWrite(authentication);
        return organizationService.createDepartment(request);
    }

    @PutMapping("/departments/{departmentId}")
    DepartmentResponse updateDepartment(Authentication authentication, @PathVariable UUID departmentId, @Valid @RequestBody DepartmentRequest request) {
        ensureDepartmentWrite(authentication);
        return organizationService.updateDepartment(departmentId, request);
    }

    @GetMapping("/positions")
    List<PositionResponse> positions(Authentication authentication) {
        ensurePositionRead(authentication);
        return organizationService.getPositions();
    }

    @PostMapping("/positions")
    @ResponseStatus(HttpStatus.CREATED)
    PositionResponse createPosition(Authentication authentication, @Valid @RequestBody PositionRequest request) {
        ensurePositionWrite(authentication);
        return organizationService.createPosition(request);
    }

    @PutMapping("/positions/{positionId}")
    PositionResponse updatePosition(Authentication authentication, @PathVariable UUID positionId, @Valid @RequestBody PositionRequest request) {
        ensurePositionWrite(authentication);
        return organizationService.updatePosition(positionId, request);
    }

    @GetMapping("/staffing")
    StaffingFilterResponse staffing(
        Authentication authentication,
        @RequestParam(name = "branchId", required = false) UUID branchId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "positionId", required = false) UUID positionId,
        @RequestParam(name = "status", required = false) StaffingUnitStatus status
    ) {
        ensureStaffingRead(authentication);
        return organizationService.getStaffingUnits(branchId, departmentId, positionId, status);
    }

    @GetMapping("/vacancies")
    List<VacancyResponse> vacancies(
        Authentication authentication,
        @RequestParam(name = "branchId", required = false) UUID branchId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "positionId", required = false) UUID positionId
    ) {
        ensureStaffingRead(authentication);
        return organizationService.getVacancies(branchId, departmentId, positionId);
    }

    private void ensureDepartmentRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DEPARTMENT", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDepartmentWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DEPARTMENT", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensurePositionRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "POSITION", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensurePositionWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "POSITION", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureStaffingRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "STAFFING", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
