WITH seed(module_code, actions) AS (
    VALUES
        ('AUTH', ARRAY['READ']::text[]),
        ('RBAC', ARRAY['READ','WRITE']::text[]),
        ('DEPARTMENTS', ARRAY['READ','WRITE']::text[]),
        ('POSITIONS', ARRAY['READ','WRITE']::text[]),
        ('STAFFING', ARRAY['READ','WRITE']::text[]),
        ('EMPLOYEES', ARRAY['READ','WRITE']::text[]),
        ('EMPLOYEE_SENSITIVE', ARRAY['READ']::text[]),
        ('EMPLOYEE_DOCUMENTS', ARRAY['READ','WRITE','DOWNLOAD']::text[]),
        ('RELATIVES', ARRAY['READ','WRITE']::text[]),
        ('ORDERS', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('LEAVES', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('TRIPS', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('SICK_LEAVES', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('ATTENDANCE', ARRAY['READ','WRITE']::text[]),
        ('SCUD', ARRAY['READ','WRITE','PROCESS']::text[]),
        ('EXPLANATIONS', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('DISCIPLINARY', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('REWARDS', ARRAY['READ','WRITE','APPROVE']::text[]),
        ('PAYROLL_EVENTS', ARRAY['READ','WRITE','EXPORT']::text[]),
        ('DISMISSALS', ARRAY['READ','WRITE','APPROVE','FINALIZE','ARCHIVE']::text[]),
        ('NOTIFICATIONS', ARRAY['READ','WRITE']::text[]),
        ('PHONE_DIRECTORY', ARRAY['READ']::text[]),
        ('LIBRARY', ARRAY['READ','WRITE']::text[]),
        ('LMS', ARRAY['READ','WRITE','ASSIGN','REPORT']::text[]),
        ('REPORTS', ARRAY['READ','EXPORT']::text[]),
        ('AUDIT', ARRAY['READ']::text[])
), expanded AS (
    SELECT module_code, unnest(actions) AS action_code
    FROM seed
)
INSERT INTO auth.permissions (
    id,
    module_code,
    action_code,
    name,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted
)
SELECT
    gen_random_uuid(),
    e.module_code,
    e.action_code,
    initcap(replace(lower(e.module_code), '_', ' ')) || ' ' || initcap(lower(e.action_code)),
    'Seeded permission for ' || lower(e.module_code) || ':' || lower(e.action_code),
    now(),
    now(),
    NULL,
    NULL,
    FALSE
FROM expanded e
WHERE NOT EXISTS (
    SELECT 1
    FROM auth.permissions p
    WHERE lower(p.module_code) = lower(e.module_code)
      AND lower(p.action_code) = lower(e.action_code)
      AND p.is_deleted = FALSE
);
