package uz.hrms.other;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.security.Permission;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.hrms.other.dto.DismissalListItemResponse;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.entity.HrNotification;
import uz.hrms.other.repository.AuditLogRepository;
import uz.hrms.other.repository.HrNotificationRepository;
import uz.hrms.other.repository.PermissionRepository;
import uz.hrms.other.service.AbsenceService;
import uz.hrms.other.service.AttendanceService;
import uz.hrms.other.service.BusinessTripService;
import uz.hrms.other.service.DismissalService;

record EmployeeSelfProfileResponse(
    UUID employeeId,
    UUID userId,
    String personnelNumber,
    String employmentStatus,
    String username,
    String firstName,
    String lastName,
    String middleName,
    String fullName,
    String email,
    UUID departmentId,
    String departmentName,
    UUID managerEmployeeId,
    String managerName
) {
}

record EmployeeSelfProfileUpdateRequest(
    @Email @Size(max = 255) String email,
    @Size(max = 100) String middleName
) {
}

record EmployeeSelfNotificationResponse(
    UUID id,
    String notificationType,
    String title,
    String body,
    String entityType,
    UUID entityId,
    NotificationStatus status,
    OffsetDateTime readAt,
    OffsetDateTime createdAt
) {
}

record EmployeeSelfFeatureAvailabilityResponse(
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

record EmployeeSelfDashboardResponse(
    EmployeeSelfProfileResponse profile,
    uz.hrms.other.AttendanceDashboardResponse attendance,
    List<uz.hrms.other.AbsenceListItemResponse> absences,
    List<uz.hrms.other.BusinessTripListItemResponse> businessTrips,
    List<uz.hrms.other.ExplanationInboxItemResponse> explanations,
    List<DismissalListItemResponse> dismissals,
    List<EmployeeSelfNotificationResponse> notifications,
    long unreadNotificationsCount,
    EmployeeSelfFeatureAvailabilityResponse features
) {
}

record EmployeeDirectoryItemResponse(
    UUID employeeId,
    String personnelNumber,
    String fullName,
    String email,
    UUID departmentId,
    String departmentName,
    UUID managerEmployeeId,
    String managerName,
    String employmentStatus
) {
}

interface EmployeeSelfServiceEmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("select e from Employee e where e.deleted = false and e.user.id = :userId")
    Optional<Employee> findByUserIdAndDeletedFalse(@Param("userId") UUID userId);

    @Query(
        "select distinct e from Employee e " +
            "left join fetch e.user u " +
            "where e.deleted = false " +
            "and e.employmentStatus in ('ACTIVE', 'ONBOARDING', 'ON_LEAVE') " +
            "order by u.lastName asc, u.firstName asc"
    )
    List<Employee> findAllActiveForDirectory();

    @Query(
        "select distinct e from Employee e " +
            "left join fetch e.user u " +
            "where e.deleted = false " +
            "and e.employmentStatus in ('ACTIVE', 'ONBOARDING', 'ON_LEAVE') " +
            "and (:query is null or lower(coalesce(u.lastName, '')) like lower(concat('%', :query, '%')) " +
            "or lower(coalesce(u.firstName, '')) like lower(concat('%', :query, '%')) " +
            "or lower(coalesce(u.middleName, '')) like lower(concat('%', :query, '%')) " +
            "or lower(coalesce(u.email, '')) like lower(concat('%', :query, '%')) " +
            "or lower(e.personnelNumber) like lower(concat('%', :query, '%'))) " +
            "order by u.lastName asc, u.firstName asc"
    )
    List<Employee> searchDirectory(@Param("query") String query);
}

interface EmployeeSelfServiceAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    @Query(
        "select a from EmployeeAssignment a " +
            "left join fetch a.department d " +
            "left join fetch a.managerEmployee m " +
            "left join fetch m.user mu " +
            "where a.deleted = false and a.employee.id = :employeeId " +
            "and a.startedAt <= :today and (a.endedAt is null or a.endedAt >= :today) " +
            "order by a.startedAt desc"
    )
    List<EmployeeAssignment> findCurrentAssignments(@Param("employeeId") UUID employeeId, @Param("today") LocalDate today);
}

interface EmployeeSelfServiceUserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByIdAndDeletedFalse(UUID id);
}

@Service
@Transactional
class EmployeeSelfService {

    private final EmployeeSelfServiceEmployeeRepository employeeRepository;
    private final EmployeeSelfServiceAssignmentRepository assignmentRepository;
    private final EmployeeSelfServiceUserAccountRepository userAccountRepository;
    private final HrNotificationRepository hrNotificationRepository;
    private final AttendanceService attendanceService;
    private final AbsenceService absenceService;
    private final BusinessTripService businessTripService;
    private final ExplanationService explanationService;
    private final DismissalService dismissalService;
    private final AuditLogRepository auditLogRepository;

    EmployeeSelfService(
        EmployeeSelfServiceEmployeeRepository employeeRepository,
        EmployeeSelfServiceAssignmentRepository assignmentRepository,
        EmployeeSelfServiceUserAccountRepository userAccountRepository,
        HrNotificationRepository hrNotificationRepository,
        AttendanceService attendanceService,
        AbsenceService absenceService,
        BusinessTripService businessTripService,
        ExplanationService explanationService,
        DismissalService dismissalService,
        AuditLogRepository auditLogRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.hrNotificationRepository = hrNotificationRepository;
        this.attendanceService = attendanceService;
        this.absenceService = absenceService;
        this.businessTripService = businessTripService;
        this.explanationService = explanationService;
        this.dismissalService = dismissalService;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    EmployeeSelfDashboardResponse dashboard(CurrentUser currentUser) {
        Employee employee = currentEmployee(currentUser);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(29);
        List<EmployeeSelfNotificationResponse> notifications = notifications(currentUser);
        long unreadCount = notifications.stream().filter(item -> item.status() == NotificationStatus.NEW).count();
        return new EmployeeSelfDashboardResponse(
            profile(currentUser),
            attendanceService.dashboard(from, to, null, employee.getId(), null),
            absenceService.list(employee.getId()),
            businessTripService.list(employee.getId()),
            explanationService.inbox(employee.getId(), null, null),
            dismissalService.list(employee.getId(), null, null),
            notifications.stream().limit(10).toList(),
            unreadCount,
            featureAvailability()
        );
    }

    @Transactional(readOnly = true)
    EmployeeSelfProfileResponse profile(CurrentUser currentUser) {
        Employee employee = currentEmployee(currentUser);
        EmployeeAssignment assignment = currentAssignment(employee.getId()).orElse(null);
        UserAccount user = employee.getUser();
        String fullName = user == null
            ? null
            : String.join(" ", user.getLastName(), user.getFirstName(), user.getMiddleName() == null ? "" : user.getMiddleName()).trim();
        String managerName = assignment == null || assignment.getManagerEmployee() == null || assignment.getManagerEmployee().getUser() == null
            ? null
            : String.join(
                " ",
                assignment.getManagerEmployee().getUser().getLastName(),
                assignment.getManagerEmployee().getUser().getFirstName(),
                assignment.getManagerEmployee().getUser().getMiddleName() == null ? "" : assignment.getManagerEmployee().getUser().getMiddleName()
            ).trim();
        return new EmployeeSelfProfileResponse(
            employee.getId(),
            user == null ? null : user.getId(),
            employee.getPersonnelNumber(),
            employee.getEmploymentStatus(),
            user == null ? null : user.getUsername(),
            user == null ? null : user.getFirstName(),
            user == null ? null : user.getLastName(),
            user == null ? null : user.getMiddleName(),
            fullName,
            user == null ? null : user.getEmail(),
            assignment == null ? null : assignment.getDepartment().getId(),
            assignment == null ? null : assignment.getDepartment().getName(),
            assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
            managerName
        );
    }

    EmployeeSelfProfileResponse updateProfile(CurrentUser currentUser, EmployeeSelfProfileUpdateRequest request) {
        Employee employee = currentEmployee(currentUser);
        UserAccount user = employee.getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee account is not linked to user");
        }
        String before = "email=" + user.getEmail() + ";middleName=" + user.getMiddleName();
        user.setEmail(trimToNull(request.email()));
        user.setMiddleName(trimToNull(request.middleName()));
        userAccountRepository.save(user);
        String after = "email=" + user.getEmail() + ";middleName=" + user.getMiddleName();
        writeAudit(currentUser, employee.getId(), "ESS_PROFILE_UPDATED", before, after);
        return profile(currentUser);
    }

    @Transactional(readOnly = true)
    List<EmployeeSelfNotificationResponse> notifications(CurrentUser currentUser) {
        Employee employee = currentEmployee(currentUser);
        return hrNotificationRepository.findAllByRecipientEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employee.getId())
            .stream()
            .map(this::toNotificationResponse)
            .toList();
    }

    EmployeeSelfNotificationResponse markNotificationRead(CurrentUser currentUser, UUID notificationId) {
        Employee employee = currentEmployee(currentUser);
        HrNotification notification = hrNotificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (notification.isDeleted() || notification.getRecipientEmployee() == null || notification.getRecipientEmployee().getId().equals(employee.getId()) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(OffsetDateTime.now());
        hrNotificationRepository.save(notification);
        writeAudit(currentUser, employee.getId(), "ESS_NOTIFICATION_READ", null, "notificationId=" + notification.getId());
        return toNotificationResponse(notification);
    }

    @Transactional(readOnly = true)
    AttendanceDashboardResponse attendance(CurrentUser currentUser, LocalDate from, LocalDate to) {
        Employee employee = currentEmployee(currentUser);
        LocalDate safeTo = to == null ? LocalDate.now() : to;
        LocalDate safeFrom = from == null ? safeTo.minusDays(29) : from;
        return attendanceService.dashboard(safeFrom, safeTo, null, employee.getId(), null);
    }

    @Transactional(readOnly = true)
    List<AbsenceListItemResponse> absences(CurrentUser currentUser) {
        return absenceService.list(currentEmployee(currentUser).getId());
    }

    @Transactional(readOnly = true)
    List<BusinessTripListItemResponse> businessTrips(CurrentUser currentUser) {
        return businessTripService.list(currentEmployee(currentUser).getId());
    }

    @Transactional(readOnly = true)
    List<ExplanationInboxItemResponse> explanations(CurrentUser currentUser) {
        return explanationService.inbox(currentEmployee(currentUser).getId(), null, null);
    }

    @Transactional(readOnly = true)
    List<DismissalListItemResponse> dismissals(CurrentUser currentUser) {
        return dismissalService.list(currentEmployee(currentUser).getId(), null, null);
    }

    @Transactional(readOnly = true)
    List<EmployeeDirectoryItemResponse> directory(String query) {
        String normalizedQuery = trimToNull(query);
        List<Employee> employees = normalizedQuery == null
            ? employeeRepository.findAllActiveForDirectory()
            : employeeRepository.searchDirectory(normalizedQuery);
        return employees.stream()
            .map(employee -> {
                EmployeeAssignment assignment = currentAssignment(employee.getId()).orElse(null);
                UserAccount user = employee.getUser();
                String fullName = user == null
                    ? null
                    : String.join(" ", user.getLastName(), user.getFirstName(), user.getMiddleName() == null ? "" : user.getMiddleName()).trim();
                String managerName = assignment == null || assignment.getManagerEmployee() == null || assignment.getManagerEmployee().getUser() == null
                    ? null
                    : String.join(
                        " ",
                        assignment.getManagerEmployee().getUser().getLastName(),
                        assignment.getManagerEmployee().getUser().getFirstName(),
                        assignment.getManagerEmployee().getUser().getMiddleName() == null ? "" : assignment.getManagerEmployee().getUser().getMiddleName()
                    ).trim();
                return new EmployeeDirectoryItemResponse(
                    employee.getId(),
                    employee.getPersonnelNumber(),
                    fullName,
                    user == null ? null : user.getEmail(),
                    assignment == null ? null : assignment.getDepartment().getId(),
                    assignment == null ? null : assignment.getDepartment().getName(),
                    assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
                    managerName,
                    employee.getEmploymentStatus()
                );
            })
            .toList();
    }

    @Transactional(readOnly = true)
    EmployeeSelfFeatureAvailabilityResponse featureAvailability() {
        return new EmployeeSelfFeatureAvailabilityResponse(
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            false,
            true,
            true,
            true,
            true,
            true,
            true,
            true
        );
    }

    private Employee currentEmployee(CurrentUser currentUser) {
        if (currentUser == null || currentUser.employeeId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employee context is required");
        }
        return employeeRepository.findByUserIdAndDeletedFalse(currentUser.userId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Employee context is required"));
    }

    private Optional<EmployeeAssignment> currentAssignment(UUID employeeId) {
        return assignmentRepository.findCurrentAssignments(employeeId, LocalDate.now()).stream().findFirst();
    }

    private EmployeeSelfNotificationResponse toNotificationResponse(HrNotification notification) {
        return new EmployeeSelfNotificationResponse(
            notification.getId(),
            notification.getNotificationType(),
            notification.getTitle(),
            notification.getBody(),
            notification.getEntityType(),
            notification.getEntityId(),
            notification.getStatus(),
            notification.getReadAt(),
            notification.getCreatedAt()
        );
    }

    private void writeAudit(CurrentUser currentUser, UUID employeeId, String action, String beforeData, String afterData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(currentUser.userId());
        auditLog.setActorEmployeeId(employeeId);
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable("employee_self_service");
        auditLog.setEntityId(employeeId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private String trimToNull(String value) {
        if (StringUtils.hasText(value) == false) {
            return null;
        }
        return value.trim();
    }
}

@Configuration
class EmployeeSelfServiceSecuritySeed {

    @Bean
    ApplicationRunner essPermissionsSeeder(
        PermissionRepository permissionRepository,
        RoleRepository roleRepository,
        RolePermissionRepository rolePermissionRepository
    ) {
        return args -> {
            Permission read = upsertPermission(permissionRepository, "ESS", "READ", "Read employee self-service dashboard");
            Permission write = upsertPermission(permissionRepository, "ESS", "WRITE", "Update employee self-service profile and notifications");

            Map<RoleCode, List<Permission>> mapping = new LinkedHashMap<>();
            mapping.put(RoleCode.SUPER_ADMIN, List.of(read, write));
            mapping.put(RoleCode.HR_ADMIN, List.of(read, write));
            mapping.put(RoleCode.HR_INSPECTOR, List.of(read));
            mapping.put(RoleCode.MANAGER, List.of(read));
            mapping.put(RoleCode.EMPLOYEE, List.of(read, write));
            mapping.put(RoleCode.TOP_MANAGEMENT, List.of(read));
            mapping.put(RoleCode.AUDITOR, List.of(read));

            for (Map.Entry<RoleCode, List<Permission>> entry : mapping.entrySet()) {
                Role role = roleRepository.findByCodeAndDeletedFalse(entry.getKey()).orElse(null);
                if (role == null) {
                    continue;
                }
                List<RolePermission> existing = rolePermissionRepository.findAllByRoleIds(List.of(role.getId()));
                for (Permission permission : entry.getValue()) {
                    boolean exists = existing.stream().anyMatch(item -> item.getPermission().authority().equals(permission.authority()));
                    if (exists) {
                        continue;
                    }
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRole(role);
                    rolePermission.setPermission(permission);
                    rolePermissionRepository.save(rolePermission);
                }
            }
        };
    }

    private Permission upsertPermission(PermissionRepository permissionRepository, String moduleCode, String actionCode, String description) {
        Permission existing = permissionRepository.findByModuleCodeAndActionCodeAndDeletedFalse(moduleCode, actionCode).orElse(null);
        if (existing != null) {
            return existing;
        }
        Permission permission = new Permission();
        permission.setModuleCode(moduleCode);
        permission.setActionCode(actionCode);
        permission.setName(moduleCode + ":" + actionCode);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }
}

@RestController
@RequestMapping("/api/v1/ess")
@Validated
@Tag(name = "Employee Self Service")
@SecurityRequirement(name = "bearerAuth")
class EmployeeSelfServiceController {

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
