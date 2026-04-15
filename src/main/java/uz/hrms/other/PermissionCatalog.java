package uz.hrms.other;

import java.util.*;
import java.util.stream.Collectors;

public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    static final List<PermissionSeed> DEFINITIONS = List.of(
            new PermissionSeed("AUTH", "READ", "Read auth profile", "Allows reading current auth context"),
            new PermissionSeed("ROLE", "READ", "Read roles", "Allows reading roles and permissions"),
            new PermissionSeed("ROLE", "WRITE", "Manage roles", "Allows managing roles and permissions"),
            new PermissionSeed("EMPLOYEE", "READ", "Read employees", "Allows reading employee data"),
            new PermissionSeed("EMPLOYEE", "WRITE", "Manage employees", "Allows creating and updating employee data"),
            new PermissionSeed("DEPARTMENT", "READ", "Read departments", "Allows reading departments"),
            new PermissionSeed("DEPARTMENT", "WRITE", "Manage departments", "Allows managing departments"),
            new PermissionSeed("POSITION", "READ", "Read positions", "Allows reading positions"),
            new PermissionSeed("POSITION", "WRITE", "Manage positions", "Allows managing positions"),
            new PermissionSeed("STAFFING", "READ", "Read staffing", "Allows reading staffing units"),
            new PermissionSeed("STAFFING", "WRITE", "Manage staffing", "Allows managing staffing units"),
            new PermissionSeed("DOCUMENT", "READ", "Read documents", "Allows reading documents"),
            new PermissionSeed("DOCUMENT", "WRITE", "Manage documents", "Allows managing documents"),
            new PermissionSeed("PAYROLL", "READ", "Read payroll", "Allows reading payroll data"),
            new PermissionSeed("PAYROLL", "WRITE", "Manage payroll", "Allows managing payroll events"),
            new PermissionSeed("ATTENDANCE", "READ", "Read attendance", "Allows reading attendance data"),
            new PermissionSeed("ATTENDANCE", "WRITE", "Manage attendance", "Allows managing attendance data"),
            new PermissionSeed("AUDIT", "READ", "Read audit logs", "Allows reading audit logs"),
            new PermissionSeed("NOTIFICATION", "READ", "Read notifications", "Allows reading notifications")
    );

    static Map<RoleCode, Set<String>> rolePermissions() {
        Map<RoleCode, Set<String>> mapping = new LinkedHashMap<>();
        Set<String> allPermissions = DEFINITIONS.stream().map(PermissionSeed::authority).collect(Collectors.toCollection(LinkedHashSet::new));
        mapping.put(RoleCode.SUPER_ADMIN, allPermissions);
        mapping.put(RoleCode.HR_ADMIN, Set.of("AUTH:READ", "ROLE:READ", "EMPLOYEE:READ", "EMPLOYEE:WRITE", "DEPARTMENT:READ", "DEPARTMENT:WRITE", "POSITION:READ", "POSITION:WRITE", "STAFFING:READ", "STAFFING:WRITE", "DOCUMENT:READ", "DOCUMENT:WRITE", "AUDIT:READ", "ATTENDANCE:READ", "PAYROLL:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.HR_INSPECTOR, Set.of("AUTH:READ", "EMPLOYEE:READ", "EMPLOYEE:WRITE", "DEPARTMENT:READ", "POSITION:READ", "POSITION:WRITE", "STAFFING:READ", "STAFFING:WRITE", "DOCUMENT:READ", "DOCUMENT:WRITE", "ATTENDANCE:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.MANAGER, Set.of("AUTH:READ", "EMPLOYEE:READ", "DEPARTMENT:READ", "POSITION:READ", "STAFFING:READ", "DOCUMENT:READ", "ATTENDANCE:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.PAYROLL_SPECIALIST, Set.of("AUTH:READ", "EMPLOYEE:READ", "POSITION:READ", "STAFFING:READ", "PAYROLL:READ", "PAYROLL:WRITE", "NOTIFICATION:READ"));
        mapping.put(RoleCode.SECURITY_OPERATOR, Set.of("AUTH:READ", "EMPLOYEE:READ", "ATTENDANCE:READ", "ATTENDANCE:WRITE"));
        mapping.put(RoleCode.EMPLOYEE, Set.of("AUTH:READ", "EMPLOYEE:READ", "DOCUMENT:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.AUDITOR, Set.of("AUTH:READ", "AUDIT:READ", "DOCUMENT:READ", "EMPLOYEE:READ", "POSITION:READ", "STAFFING:READ", "DEPARTMENT:READ"));
        mapping.put(RoleCode.TOP_MANAGEMENT, Set.of("AUTH:READ", "EMPLOYEE:READ", "DEPARTMENT:READ", "POSITION:READ", "STAFFING:READ", "DOCUMENT:READ", "AUDIT:READ", "PAYROLL:READ", "ATTENDANCE:READ"));
        return mapping;
    }
}
