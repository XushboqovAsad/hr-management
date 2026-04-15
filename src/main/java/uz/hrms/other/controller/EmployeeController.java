package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeQueryService employeeQueryService;
    private final EmployeeCommandService employeeCommandService;

    EmployeeController(EmployeeQueryService employeeQueryService,
                       EmployeeCommandService employeeCommandService) {
        this.employeeQueryService = employeeQueryService;
        this.employeeCommandService = employeeCommandService;
    }

    @GetMapping
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'READ')")
    @Operation(summary = "List employees with scope-aware filtering")
    ResponseEntity<List<EmployeeListItemResponse>> list(Authentication authentication,
                                                        @RequestParam(name = "departmentId", required = false) UUID departmentId,
                                                        @RequestParam(name = "status", required = false) String status,
                                                        @RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(employeeQueryService.list(authentication, departmentId, status, query));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #p2)")
    @Operation(summary = "Get employee card")
    ResponseEntity<EmployeeCardResponse> get(Authentication authentication,
                                             HttpServletRequest request,
                                             @PathVariable("employeeId") UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getEmployee(authentication, request.getRequestURI(), employeeId));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #p2)")
    @Operation(summary = "Get employee history")
    ResponseEntity<List<EmployeeHistoryItemResponse>> history(Authentication authentication,
                                                              HttpServletRequest request,
                                                              @PathVariable("employeeId") UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getHistory(authentication, request.getRequestURI(), employeeId));
    }

    @GetMapping("/{employeeId}/profile")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #p2)")
    @Operation(summary = "Get employee profile with scope-aware authorization")
    ResponseEntity<EmployeeProfileResponse> getProfile(Authentication authentication,
                                                       HttpServletRequest request,
                                                       @PathVariable("employeeId") UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getProfile(authentication, request.getRequestURI(), employeeId));
    }

    @PostMapping
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'WRITE')")
    @Operation(summary = "Create employee")
    ResponseEntity<EmployeeCardResponse> create(Authentication authentication,
                                                @Valid @RequestBody EmployeeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeCommandService.create(authentication, request));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'WRITE')")
    @Operation(summary = "Update employee")
    ResponseEntity<EmployeeCardResponse> update(Authentication authentication,
                                                @PathVariable("employeeId") UUID employeeId,
                                                @Valid @RequestBody EmployeeUpsertRequest request) {
        return ResponseEntity.ok(employeeCommandService.update(authentication, employeeId, request));
    }

    @PatchMapping("/{employeeId}/status")
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'WRITE')")
    @Operation(summary = "Change employee status")
    ResponseEntity<EmployeeCardResponse> changeStatus(Authentication authentication,
                                                      @PathVariable("employeeId") UUID employeeId,
                                                      @Valid @RequestBody EmployeeStatusChangeRequest request) {
        return ResponseEntity.ok(employeeCommandService.changeStatus(authentication, employeeId, request));
    }
