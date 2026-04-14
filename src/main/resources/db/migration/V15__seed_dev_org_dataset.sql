INSERT INTO hr.departments (
    id,
    code,
    name,
    parent_department_id,
    is_active,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted,
    unit_type,
    manager_employee_id,
    phone,
    email,
    location,
    closed_at
)
SELECT
    seed.id,
    seed.code,
    seed.name,
    seed.parent_department_id,
    TRUE,
    now(),
    now(),
    NULL,
    NULL,
    FALSE,
    seed.unit_type,
    NULL,
    seed.phone,
    seed.email,
    seed.location,
    NULL
FROM (
    VALUES
        ('10000000-0000-0000-0000-000000000001'::uuid, 'DEV-HQ', 'Dev Head Office', NULL::uuid, 'BRANCH', '+998-71-200-00-01', 'hq.dev@hrms.local', 'Tashkent HQ'),
        ('10000000-0000-0000-0000-000000000002'::uuid, 'DEV-HR', 'Dev HR Department', '10000000-0000-0000-0000-000000000001'::uuid, 'DEPARTMENT', '+998-71-200-01-00', 'hr.dev@hrms.local', 'Floor 2'),
        ('10000000-0000-0000-0000-000000000003'::uuid, 'DEV-FIN', 'Dev Finance Department', '10000000-0000-0000-0000-000000000001'::uuid, 'DEPARTMENT', '+998-71-200-02-00', 'fin.dev@hrms.local', 'Floor 3'),
        ('10000000-0000-0000-0000-000000000004'::uuid, 'DEV-SEC', 'Dev Security Department', '10000000-0000-0000-0000-000000000001'::uuid, 'DEPARTMENT', '+998-71-200-03-00', 'sec.dev@hrms.local', 'Floor 1'),
        ('10000000-0000-0000-0000-000000000005'::uuid, 'DEV-COMM', 'Dev Commercial Department', '10000000-0000-0000-0000-000000000001'::uuid, 'DEPARTMENT', '+998-71-200-04-00', 'comm.dev@hrms.local', 'Floor 4'),
        ('10000000-0000-0000-0000-000000000006'::uuid, 'DEV-SALES-MGMT', 'Dev Sales Management', '10000000-0000-0000-0000-000000000005'::uuid, 'MANAGEMENT', '+998-71-200-04-10', 'sales-mgmt.dev@hrms.local', 'Floor 4'),
        ('10000000-0000-0000-0000-000000000007'::uuid, 'DEV-SALES-SEC', 'Dev Sales Sector', '10000000-0000-0000-0000-000000000006'::uuid, 'SECTOR', '+998-71-200-04-11', 'sales.dev@hrms.local', 'Floor 4'),
        ('10000000-0000-0000-0000-000000000008'::uuid, 'DEV-IT', 'Dev IT Department', '10000000-0000-0000-0000-000000000001'::uuid, 'DEPARTMENT', '+998-71-200-05-00', 'it.dev@hrms.local', 'Floor 5')
) AS seed(id, code, name, parent_department_id, unit_type, phone, email, location)
WHERE NOT EXISTS (
    SELECT 1 FROM hr.departments d WHERE lower(d.code) = lower(seed.code) AND d.is_deleted = FALSE
);

INSERT INTO hr.positions (
    id,
    code,
    title,
    category,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    seed.id,
    seed.code,
    seed.title,
    seed.category,
    TRUE,
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM (
    VALUES
        ('20000000-0000-0000-0000-000000000001'::uuid, 'DEV-CEO', 'Chief Executive Officer', 'Executive'),
        ('20000000-0000-0000-0000-000000000002'::uuid, 'DEV-HR-ADMIN', 'HR Administrator', 'HR'),
        ('20000000-0000-0000-0000-000000000003'::uuid, 'DEV-HR-INSPECTOR', 'HR Inspector', 'HR'),
        ('20000000-0000-0000-0000-000000000004'::uuid, 'DEV-PAYROLL', 'Payroll Specialist', 'Finance'),
        ('20000000-0000-0000-0000-000000000005'::uuid, 'DEV-SEC-OP', 'Security Operator', 'Security'),
        ('20000000-0000-0000-0000-000000000006'::uuid, 'DEV-SALES-MANAGER', 'Sales Manager', 'Commercial'),
        ('20000000-0000-0000-0000-000000000007'::uuid, 'DEV-SALES-SPECIALIST', 'Sales Specialist', 'Commercial'),
        ('20000000-0000-0000-0000-000000000008'::uuid, 'DEV-AUDITOR', 'Internal Auditor', 'Audit'),
        ('20000000-0000-0000-0000-000000000009'::uuid, 'DEV-TOP-MGMT', 'Executive Director', 'Executive'),
        ('20000000-0000-0000-0000-000000000010'::uuid, 'DEV-IT-SPECIALIST', 'IT Specialist', 'IT')
) AS seed(id, code, title, category)
WHERE NOT EXISTS (
    SELECT 1 FROM hr.positions p WHERE lower(p.code) = lower(seed.code) AND p.is_deleted = FALSE
);

INSERT INTO hr.staffing_units (
    id,
    code,
    department_id,
    position_id,
    planned_fte,
    occupied_fte,
    status,
    opened_at,
    closed_at,
    notes,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    seed.id,
    seed.code,
    d.id,
    p.id,
    seed.planned_fte,
    seed.occupied_fte,
    seed.status,
    DATE '2026-01-01',
    NULL,
    seed.notes,
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM (
    VALUES
        ('30000000-0000-0000-0000-000000000001'::uuid, 'DEV-STF-CEO', 'DEV-HQ', 'DEV-CEO', 1.00, 1.00, 'ACTIVE', 'Executive headcount'),
        ('30000000-0000-0000-0000-000000000002'::uuid, 'DEV-STF-HR-ADMIN', 'DEV-HR', 'DEV-HR-ADMIN', 1.00, 1.00, 'ACTIVE', 'HR administration'),
        ('30000000-0000-0000-0000-000000000003'::uuid, 'DEV-STF-HR-INSP', 'DEV-HR', 'DEV-HR-INSPECTOR', 1.00, 1.00, 'ACTIVE', 'HR operations'),
        ('30000000-0000-0000-0000-000000000004'::uuid, 'DEV-STF-PAYROLL', 'DEV-FIN', 'DEV-PAYROLL', 1.00, 1.00, 'ACTIVE', 'Payroll operations'),
        ('30000000-0000-0000-0000-000000000005'::uuid, 'DEV-STF-SEC', 'DEV-SEC', 'DEV-SEC-OP', 1.00, 1.00, 'ACTIVE', 'Security operations'),
        ('30000000-0000-0000-0000-000000000006'::uuid, 'DEV-STF-SALES-MGR', 'DEV-SALES-MGMT', 'DEV-SALES-MANAGER', 1.00, 1.00, 'ACTIVE', 'Sales management'),
        ('30000000-0000-0000-0000-000000000007'::uuid, 'DEV-STF-SALES-1', 'DEV-SALES-SEC', 'DEV-SALES-SPECIALIST', 1.00, 1.00, 'ACTIVE', 'Occupied sales specialist seat'),
        ('30000000-0000-0000-0000-000000000008'::uuid, 'DEV-STF-SALES-2', 'DEV-SALES-SEC', 'DEV-SALES-SPECIALIST', 1.00, 0.00, 'ACTIVE', 'Vacant sales specialist seat'),
        ('30000000-0000-0000-0000-000000000009'::uuid, 'DEV-STF-AUDITOR', 'DEV-HQ', 'DEV-AUDITOR', 1.00, 1.00, 'ACTIVE', 'Internal audit seat'),
        ('30000000-0000-0000-0000-000000000010'::uuid, 'DEV-STF-TOP-MGMT', 'DEV-HQ', 'DEV-TOP-MGMT', 1.00, 1.00, 'ACTIVE', 'Executive management seat'),
        ('30000000-0000-0000-0000-000000000011'::uuid, 'DEV-STF-IT-1', 'DEV-IT', 'DEV-IT-SPECIALIST', 1.00, 0.00, 'ACTIVE', 'Vacant IT specialist seat')
) AS seed(id, code, department_code, position_code, planned_fte, occupied_fte, status, notes)
JOIN hr.departments d ON lower(d.code) = lower(seed.department_code) AND d.is_deleted = FALSE
JOIN hr.positions p ON lower(p.code) = lower(seed.position_code) AND p.is_deleted = FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM hr.staffing_units su WHERE lower(su.code) = lower(seed.code) AND su.is_deleted = FALSE
);
