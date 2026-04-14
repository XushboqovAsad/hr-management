-- All users below share password: ChangeMe123!
-- bcrypt: $2y$10$uOJVYDfIiJxWCmQtAyJ..OyACXNG2y02EiC2NB1.hsITdGthnsV6C

INSERT INTO auth.users (
    id,
    username,
    password_hash,
    email,
    first_name,
    last_name,
    middle_name,
    is_active,
    last_login_at,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    seed.id,
    seed.username,
    '$2y$10$uOJVYDfIiJxWCmQtAyJ..OyACXNG2y02EiC2NB1.hsITdGthnsV6C',
    seed.email,
    seed.first_name,
    seed.last_name,
    seed.middle_name,
    TRUE,
    NULL,
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM (
    VALUES
        ('40000000-0000-0000-0000-000000000001'::uuid, 'dev.superadmin', 'dev.superadmin@hrms.local', 'Dev', 'Superadmin', NULL),
        ('40000000-0000-0000-0000-000000000002'::uuid, 'dev.hr.admin', 'dev.hr.admin@hrms.local', 'Aziza', 'Karimova', NULL),
        ('40000000-0000-0000-0000-000000000003'::uuid, 'dev.hr.inspector', 'dev.hr.inspector@hrms.local', 'Dilnoza', 'Rakhimova', NULL),
        ('40000000-0000-0000-0000-000000000004'::uuid, 'dev.manager.sales', 'dev.manager.sales@hrms.local', 'Jasur', 'Toshpulatov', NULL),
        ('40000000-0000-0000-0000-000000000005'::uuid, 'dev.payroll', 'dev.payroll@hrms.local', 'Madina', 'Saidova', NULL),
        ('40000000-0000-0000-0000-000000000006'::uuid, 'dev.security', 'dev.security@hrms.local', 'Bekzod', 'Ismoilov', NULL),
        ('40000000-0000-0000-0000-000000000007'::uuid, 'dev.employee', 'dev.employee@hrms.local', 'Umid', 'Ergashev', NULL),
        ('40000000-0000-0000-0000-000000000008'::uuid, 'dev.auditor', 'dev.auditor@hrms.local', 'Nigora', 'Yuldasheva', NULL),
        ('40000000-0000-0000-0000-000000000009'::uuid, 'dev.top.management', 'dev.top.management@hrms.local', 'Akmal', 'Khasanov', NULL)
) AS seed(id, username, email, first_name, last_name, middle_name)
WHERE NOT EXISTS (
    SELECT 1 FROM auth.users u WHERE lower(u.username) = lower(seed.username) AND u.is_deleted = FALSE
);

INSERT INTO hr.employees (
    id,
    user_id,
    personnel_number,
    status,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    seed.id,
    u.id,
    seed.personnel_number,
    'ACTIVE',
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM (
    VALUES
        ('50000000-0000-0000-0000-000000000002'::uuid, 'dev.hr.admin', 'DEV-EMP-0002'),
        ('50000000-0000-0000-0000-000000000003'::uuid, 'dev.hr.inspector', 'DEV-EMP-0003'),
        ('50000000-0000-0000-0000-000000000004'::uuid, 'dev.manager.sales', 'DEV-EMP-0004'),
        ('50000000-0000-0000-0000-000000000005'::uuid, 'dev.payroll', 'DEV-EMP-0005'),
        ('50000000-0000-0000-0000-000000000006'::uuid, 'dev.security', 'DEV-EMP-0006'),
        ('50000000-0000-0000-0000-000000000007'::uuid, 'dev.employee', 'DEV-EMP-0007'),
        ('50000000-0000-0000-0000-000000000008'::uuid, 'dev.auditor', 'DEV-EMP-0008'),
        ('50000000-0000-0000-0000-000000000009'::uuid, 'dev.top.management', 'DEV-EMP-0009')
) AS seed(id, username, personnel_number)
JOIN auth.users u ON lower(u.username) = lower(seed.username) AND u.is_deleted = FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM hr.employees e WHERE lower(e.personnel_number) = lower(seed.personnel_number) AND e.is_deleted = FALSE
);

INSERT INTO hr.employee_assignments (
    id,
    employee_id,
    department_id,
    manager_employee_id,
    is_primary,
    effective_from,
    effective_to,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted,
    position_id,
    staffing_unit_id
)
SELECT
    seed.id,
    e.id,
    d.id,
    mgr.id,
    TRUE,
    DATE '2026-01-01',
    NULL,
    now(),
    now(),
    NULL,
    NULL,
    FALSE,
    p.id,
    su.id
FROM (
    VALUES
        ('60000000-0000-0000-0000-000000000002'::uuid, 'DEV-EMP-0002', 'DEV-HR', NULL, 'DEV-HR-ADMIN', 'DEV-STF-HR-ADMIN'),
        ('60000000-0000-0000-0000-000000000003'::uuid, 'DEV-EMP-0003', 'DEV-HR', 'DEV-EMP-0002', 'DEV-HR-INSPECTOR', 'DEV-STF-HR-INSP'),
        ('60000000-0000-0000-0000-000000000004'::uuid, 'DEV-EMP-0004', 'DEV-SALES-MGMT', 'DEV-EMP-0009', 'DEV-SALES-MANAGER', 'DEV-STF-SALES-MGR'),
        ('60000000-0000-0000-0000-000000000005'::uuid, 'DEV-EMP-0005', 'DEV-FIN', 'DEV-EMP-0009', 'DEV-PAYROLL', 'DEV-STF-PAYROLL'),
        ('60000000-0000-0000-0000-000000000006'::uuid, 'DEV-EMP-0006', 'DEV-SEC', 'DEV-EMP-0009', 'DEV-SEC-OP', 'DEV-STF-SEC'),
        ('60000000-0000-0000-0000-000000000007'::uuid, 'DEV-EMP-0007', 'DEV-SALES-SEC', 'DEV-EMP-0004', 'DEV-SALES-SPECIALIST', 'DEV-STF-SALES-1'),
        ('60000000-0000-0000-0000-000000000008'::uuid, 'DEV-EMP-0008', 'DEV-HQ', 'DEV-EMP-0009', 'DEV-AUDITOR', 'DEV-STF-AUDITOR'),
        ('60000000-0000-0000-0000-000000000009'::uuid, 'DEV-EMP-0009', 'DEV-HQ', NULL, 'DEV-TOP-MGMT', 'DEV-STF-TOP-MGMT')
) AS seed(id, personnel_number, department_code, manager_personnel_number, position_code, staffing_code)
JOIN hr.employees e ON lower(e.personnel_number) = lower(seed.personnel_number) AND e.is_deleted = FALSE
JOIN hr.departments d ON lower(d.code) = lower(seed.department_code) AND d.is_deleted = FALSE
JOIN hr.positions p ON lower(p.code) = lower(seed.position_code) AND p.is_deleted = FALSE
JOIN hr.staffing_units su ON lower(su.code) = lower(seed.staffing_code) AND su.is_deleted = FALSE
LEFT JOIN hr.employees mgr ON seed.manager_personnel_number IS NOT NULL AND lower(mgr.personnel_number) = lower(seed.manager_personnel_number) AND mgr.is_deleted = FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM hr.employee_assignments ea
    WHERE ea.employee_id = e.id
      AND ea.is_primary = TRUE
      AND ea.effective_to IS NULL
      AND ea.is_deleted = FALSE
);

UPDATE hr.departments d
SET manager_employee_id = e.id
FROM hr.employees e
WHERE (lower(d.code), lower(e.personnel_number)) IN (
    ('dev-hr', 'dev-emp-0002'),
    ('dev-fin', 'dev-emp-0005'),
    ('dev-sec', 'dev-emp-0006'),
    ('dev-sales-mgmt', 'dev-emp-0004'),
    ('dev-sales-sec', 'dev-emp-0004'),
    ('dev-hq', 'dev-emp-0009')
);

WITH seed(username, role_code, scope_type, scope_department_code) AS (
    VALUES
        ('dev.superadmin', 'SUPER_ADMIN', 'GLOBAL', NULL),
        ('dev.hr.admin', 'HR_ADMIN', 'GLOBAL', NULL),
        ('dev.hr.admin', 'EMPLOYEE', 'SELF', NULL),
        ('dev.hr.inspector', 'HR_INSPECTOR', 'GLOBAL', NULL),
        ('dev.hr.inspector', 'EMPLOYEE', 'SELF', NULL),
        ('dev.manager.sales', 'MANAGER', 'DEPARTMENT', 'DEV-SALES-MGMT'),
        ('dev.manager.sales', 'EMPLOYEE', 'SELF', NULL),
        ('dev.payroll', 'PAYROLL_SPECIALIST', 'GLOBAL', NULL),
        ('dev.payroll', 'EMPLOYEE', 'SELF', NULL),
        ('dev.security', 'SECURITY_OPERATOR', 'GLOBAL', NULL),
        ('dev.security', 'EMPLOYEE', 'SELF', NULL),
        ('dev.employee', 'EMPLOYEE', 'SELF', NULL),
        ('dev.auditor', 'AUDITOR', 'GLOBAL', NULL),
        ('dev.auditor', 'EMPLOYEE', 'SELF', NULL),
        ('dev.top.management', 'TOP_MANAGEMENT', 'GLOBAL', NULL),
        ('dev.top.management', 'EMPLOYEE', 'SELF', NULL)
)
INSERT INTO auth.user_roles (
    id,
    user_id,
    role_id,
    scope_type,
    scope_department_id,
    is_active,
    valid_from,
    valid_to,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    gen_random_uuid(),
    u.id,
    r.id,
    seed.scope_type,
    d.id,
    TRUE,
    DATE '2026-01-01',
    NULL,
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM seed
JOIN auth.users u ON lower(u.username) = lower(seed.username) AND u.is_deleted = FALSE
JOIN auth.roles r ON r.code = seed.role_code AND r.is_deleted = FALSE
LEFT JOIN hr.departments d ON seed.scope_department_code IS NOT NULL AND lower(d.code) = lower(seed.scope_department_code) AND d.is_deleted = FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM auth.user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
      AND ur.scope_type = seed.scope_type
      AND ((ur.scope_department_id IS NULL AND d.id IS NULL) OR ur.scope_department_id = d.id)
      AND ur.is_deleted = FALSE
);
