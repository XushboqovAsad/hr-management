UPDATE hr.departments
SET manager_employee_id = NULL
WHERE lower(code) LIKE 'dev-%';

DELETE FROM auth.user_roles
WHERE user_id IN (
    SELECT id
    FROM auth.users
    WHERE lower(username) IN (
        'dev.superadmin',
        'dev.hr.admin',
        'dev.hr.inspector',
        'dev.manager.sales',
        'dev.payroll',
        'dev.security',
        'dev.employee',
        'dev.auditor',
        'dev.top.management'
    )
);

DELETE FROM hr.employee_assignments
WHERE employee_id IN (
    SELECT id
    FROM hr.employees
    WHERE lower(personnel_number) LIKE 'dev-emp-%'
);

DELETE FROM hr.employees
WHERE lower(personnel_number) LIKE 'dev-emp-%';

DELETE FROM auth.users
WHERE lower(username) IN (
    'dev.superadmin',
    'dev.hr.admin',
    'dev.hr.inspector',
    'dev.manager.sales',
    'dev.payroll',
    'dev.security',
    'dev.employee',
    'dev.auditor',
    'dev.top.management'
);

DELETE FROM hr.staffing_units
WHERE lower(code) LIKE 'dev-stf-%';

DELETE FROM hr.positions
WHERE lower(code) LIKE 'dev-%';

DELETE FROM hr.departments
WHERE lower(code) LIKE 'dev-%';
