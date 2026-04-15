package uz.hrms.auth.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    public record PermissionDefinition(String moduleCode, String actionCode, String name, String description) {
        public String authority() {
            return moduleCode + ":" + actionCode;
        }
    }

    public static final List<PermissionDefinition> DEFINITIONS = List.of(
            new PermissionDefinition("AUTH", "READ", "Read auth profile", "Allows reading current auth context"),
            new PermissionDefinition("ROLE", "READ", "Read roles", "Allows reading roles and permissions"),
            new PermissionDefinition("ROLE", "WRITE", "Manage roles", "Allows managing roles and permissions"),
            new PermissionDefinition("EMPLOYEE", "READ", "Read employees", "Allows reading employee data"),
            new PermissionDefinition("EMPLOYEE", "WRITE", "Manage employees", "Allows creating and updating employee data"),
            new PermissionDefinition("EMPLOYEE_SENSITIVE", "READ", "Read sensitive employee fields", "Allows reading sensitive employee fields without masking"),
            new PermissionDefinition("DEPARTMENT", "READ", "Read departments", "Allows reading departments"),
            new PermissionDefinition("DEPARTMENT", "WRITE", "Manage departments", "Allows managing departments"),
            new PermissionDefinition("DOCUMENT", "READ", "Read documents", "Allows reading documents"),
            new PermissionDefinition("DOCUMENT", "WRITE", "Manage documents", "Allows managing documents"),
            new PermissionDefinition("FILE", "DOWNLOAD", "Download protected files", "Allows downloading and previewing protected files"),
            new PermissionDefinition("PAYROLL", "READ", "Read payroll", "Allows reading payroll data"),
            new PermissionDefinition("PAYROLL", "WRITE", "Manage payroll", "Allows managing payroll events"),
            new PermissionDefinition("ATTENDANCE", "READ", "Read attendance", "Allows reading attendance data"),
            new PermissionDefinition("ATTENDANCE", "WRITE", "Manage attendance", "Allows managing attendance data"),
            new PermissionDefinition("AUDIT", "READ", "Read audit logs", "Allows reading audit logs"),
            new PermissionDefinition("NOTIFICATION", "READ", "Read notifications", "Allows reading notifications")
    );

    public static Map<RoleCode, Set<String>> rolePermissions() {
        Map<RoleCode, Set<String>> mapping = new LinkedHashMap<>();
        Set<String> allPermissions = DEFINITIONS.stream().map(PermissionDefinition::authority).collect(Collectors.toCollection(LinkedHashSet::new));
        mapping.put(RoleCode.SUPER_ADMIN, allPermissions);
        mapping.put(RoleCode.HR_ADMIN, Set.of(
                "AUTH:READ", "ROLE:READ", "EMPLOYEE:READ", "EMPLOYEE:WRITE", "EMPLOYEE_SENSITIVE:READ",
                "DEPARTMENT:READ", "DEPARTMENT:WRITE", "DOCUMENT:READ", "DOCUMENT:WRITE", "FILE:DOWNLOAD",
                "AUDIT:READ", "ATTENDANCE:READ", "PAYROLL:READ", "NOTIFICATION:READ"
        ));
        mapping.put(RoleCode.HR_INSPECTOR, Set.of(
                "AUTH:READ", "EMPLOYEE:READ", "EMPLOYEE:WRITE", "EMPLOYEE_SENSITIVE:READ", "DEPARTMENT:READ",
                "DOCUMENT:READ", "DOCUMENT:WRITE", "FILE:DOWNLOAD", "ATTENDANCE:READ", "NOTIFICATION:READ"
        ));
        mapping.put(RoleCode.MANAGER, Set.of(
                "AUTH:READ", "EMPLOYEE:READ", "EMPLOYEE_SENSITIVE:READ", "DEPARTMENT:READ", "DOCUMENT:READ", "FILE:DOWNLOAD",
                "ATTENDANCE:READ", "NOTIFICATION:READ"
        ));
        mapping.put(RoleCode.PAYROLL_SPECIALIST, Set.of(
                "AUTH:READ", "EMPLOYEE:READ", "PAYROLL:READ", "PAYROLL:WRITE", "NOTIFICATION:READ"
        ));
        mapping.put(RoleCode.SECURITY_OPERATOR, Set.of(
                "AUTH:READ", "EMPLOYEE:READ", "ATTENDANCE:READ", "ATTENDANCE:WRITE"
        ));
        mapping.put(RoleCode.EMPLOYEE, Set.of(
                "AUTH:READ", "EMPLOYEE:READ", "DOCUMENT:READ", "FILE:DOWNLOAD", "NOTIFICATION:READ"
        ));
        mapping.put(RoleCode.AUDITOR, Set.of(
                "AUTH:READ", "AUDIT:READ", "DOCUMENT:READ", "FILE:DOWNLOAD", "EMPLOYEE:READ"
        ));
        mapping.put(RoleCode.TOP_MANAGEMENT, Set.of(
                "AUTH:READ", "EMPLOYEE:READ", "EMPLOYEE_SENSITIVE:READ", "DEPARTMENT:READ", "DOCUMENT:READ", "FILE:DOWNLOAD",
                "AUDIT:READ", "PAYROLL:READ", "ATTENDANCE:READ"
        ));
        return mapping;
    }
}
