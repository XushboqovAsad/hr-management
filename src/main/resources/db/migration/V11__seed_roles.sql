INSERT INTO auth.roles (
    id,
    code,
    name,
    description,
    is_system,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    gen_random_uuid(),
    seed.code,
    seed.name,
    seed.description,
    TRUE,
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM (
    VALUES
        ('SUPER_ADMIN', 'Super Admin', 'Full system administration'),
        ('HR_ADMIN', 'HR Admin', 'Full HR administration access'),
        ('HR_INSPECTOR', 'HR Inspector', 'Operational HR specialist'),
        ('MANAGER', 'Manager', 'Department manager access'),
        ('PAYROLL_SPECIALIST', 'Payroll Specialist', 'Payroll export and basis processing'),
        ('SECURITY_OPERATOR', 'Security Operator', 'Attendance and SCUD operations'),
        ('EMPLOYEE', 'Employee', 'Self-service employee access'),
        ('AUDITOR', 'Auditor', 'Read-only audit and compliance access'),
        ('TOP_MANAGEMENT', 'Top Management', 'Executive reporting access')
) AS seed(code, name, description)
WHERE NOT EXISTS (
    SELECT 1
    FROM auth.roles r
    WHERE lower(r.code) = lower(seed.code)
      AND r.is_deleted = FALSE
);
