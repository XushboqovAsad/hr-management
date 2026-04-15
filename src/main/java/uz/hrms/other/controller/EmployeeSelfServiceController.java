package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.AccessPolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ess")
@Validated
@Tag(name = "Employee Self Service")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeSelfServiceController {

    private final EmployeeSelfService employeeSelfService;
    private final AccessPolicy accessPolicy;

    EmployeeSelfServiceController(EmployeeSelfService employeeSelfService, AccessPolicy accessPolicy) {
        this.employeeSelfService = employeeSelfService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping("/me/dashboard")
    @Operation(summary = "Employee self-service dashboard")
    ResponseEntity<EmployeeSelfDashboardResponse> dashboard(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.dashboard(currentUser));
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Employee self profile")
    ResponseEntity<EmployeeSelfProfileResponse> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.profile(currentUser));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update limited self profile fields")
    ResponseEntity<EmployeeSelfProfileResponse> updateProfile(@AuthenticationPrincipal CurrentUser currentUser, @Valid @RequestBody EmployeeSelfProfileUpdateRequest request) {
        ensureEssWrite(currentUser);
        return ResponseEntity.ok(employeeSelfService.updateProfile(currentUser, request));
    }

    @GetMapping("/me/notifications")
    @Operation(summary = "Employee notifications center")
    ResponseEntity<List<EmployeeSelfNotificationResponse>> notifications(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.notifications(currentUser));
    }

    @PostMapping("/me/notifications/{notificationId}/read")
    @Operation(summary = "Mark employee notification as read")
    ResponseEntity<EmployeeSelfNotificationResponse> markRead(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable UUID notificationId) {
        ensureEssWrite(currentUser);
        return ResponseEntity.ok(employeeSelfService.markNotificationRead(currentUser, notificationId));
    }

    @GetMapping("/me/attendance")
    @Operation(summary = "Employee attendance view")
    ResponseEntity<AttendanceDashboardResponse> attendance(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to
    ) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.attendance(currentUser, from, to));
    }

    @GetMapping("/me/absences")
    @Operation(summary = "Employee absences")
    ResponseEntity<List<AbsenceListItemResponse>> absences(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.absences(currentUser));
    }

    @GetMapping("/me/business-trips")
    @Operation(summary = "Employee business trips")
    ResponseEntity<List<BusinessTripListItemResponse>> businessTrips(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.businessTrips(currentUser));
    }

    @GetMapping("/me/explanations")
    @Operation(summary = "Employee explanation tasks")
    ResponseEntity<List<ExplanationInboxItemResponse>> explanations(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.explanations(currentUser));
    }

    @GetMapping("/me/dismissals")
    @Operation(summary = "Employee dismissals")
    ResponseEntity<List<DismissalListItemResponse>> dismissals(@AuthenticationPrincipal CurrentUser currentUser) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.dismissals(currentUser));
    }

    @GetMapping("/directory")
    @Operation(summary = "Phone directory")
    ResponseEntity<List<EmployeeDirectoryItemResponse>> directory(@AuthenticationPrincipal CurrentUser currentUser, @RequestParam(name = "query", required = false) String query) {
        ensureEssRead(currentUser);
        return ResponseEntity.ok(employeeSelfService.directory(query));
    }

    private void ensureEssRead(CurrentUser currentUser) {
        if (currentUser == null || accessPolicy.hasPermission(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities()), "ESS", "READ") == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private void ensureEssWrite(CurrentUser currentUser) {
        if (currentUser == null || accessPolicy.hasPermission(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities()), "ESS", "WRITE") == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
