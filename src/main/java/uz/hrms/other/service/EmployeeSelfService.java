package uz.hrms.other.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.ExplanationService;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.entity.HrNotification;
import uz.hrms.other.repository.AuditLogRepository;
import uz.hrms.other.repository.HrNotificationRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmployeeSelfService {

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
