-- Run only in disposable environments.
-- Recommended order:
--   1. rollback dev dataset first
--   2. rollback role mappings
--   3. rollback permissions and roles
--   4. rollback reference values only if they are not already used by business data

DELETE FROM auth.role_permissions rp
USING auth.roles r
WHERE rp.role_id = r.id
  AND r.code IN (
      'SUPER_ADMIN',
      'HR_ADMIN',
      'HR_INSPECTOR',
      'MANAGER',
      'PAYROLL_SPECIALIST',
      'SECURITY_OPERATOR',
      'EMPLOYEE',
      'AUDITOR',
      'TOP_MANAGEMENT'
  );

DELETE FROM auth.user_roles ur
USING auth.roles r
WHERE ur.role_id = r.id
  AND r.code IN (
      'SUPER_ADMIN',
      'HR_ADMIN',
      'HR_INSPECTOR',
      'MANAGER',
      'PAYROLL_SPECIALIST',
      'SECURITY_OPERATOR',
      'EMPLOYEE',
      'AUDITOR',
      'TOP_MANAGEMENT'
  );

DELETE FROM auth.permissions
WHERE module_code IN (
    'AUTH', 'RBAC', 'DEPARTMENTS', 'POSITIONS', 'STAFFING', 'EMPLOYEES',
    'EMPLOYEE_SENSITIVE', 'EMPLOYEE_DOCUMENTS', 'RELATIVES', 'ORDERS', 'LEAVES',
    'TRIPS', 'SICK_LEAVES', 'ATTENDANCE', 'SCUD', 'EXPLANATIONS', 'DISCIPLINARY',
    'REWARDS', 'PAYROLL_EVENTS', 'DISMISSALS', 'NOTIFICATIONS', 'PHONE_DIRECTORY',
    'LIBRARY', 'LMS', 'REPORTS', 'AUDIT'
)
AND is_deleted = FALSE;

DELETE FROM auth.roles
WHERE code IN (
    'SUPER_ADMIN',
    'HR_ADMIN',
    'HR_INSPECTOR',
    'MANAGER',
    'PAYROLL_SPECIALIST',
    'SECURITY_OPERATOR',
    'EMPLOYEE',
    'AUDITOR',
    'TOP_MANAGEMENT'
)
AND is_deleted = FALSE;

DELETE FROM ref.order_types WHERE code IN ('HIRE', 'TRANSFER', 'SALARY_CHANGE', 'LEAVE', 'BUSINESS_TRIP', 'DISCIPLINARY_ACTION', 'REWARD', 'DISMISSAL');
DELETE FROM ref.leave_types WHERE code IN ('ANNUAL', 'ADDITIONAL', 'UNPAID', 'STUDY', 'MATERNITY', 'CHILDCARE', 'SOCIAL');
DELETE FROM ref.disciplinary_action_types WHERE code IN ('REMARK', 'REPRIMAND', 'SEVERE_REPRIMAND');
DELETE FROM ref.reward_types WHERE code IN ('THANKS', 'AWARD', 'CERTIFICATE', 'BONUS');
DELETE FROM ref.document_types WHERE code IN ('PASSPORT', 'LABOR_CONTRACT', 'HIRE_ORDER', 'DISMISSAL_ORDER', 'DIPLOMA', 'CERTIFICATION', 'SICK_LEAVE_CERT', 'BUSINESS_TRIP_REPORT', 'EXPLANATION_NOTE', 'OTHER');
DELETE FROM ref.notification_types WHERE code IN ('TASK_ASSIGNED', 'APPROVAL_REQUEST', 'APPROVAL_RESULT', 'BIRTHDAY_REMINDER', 'ORDER_ACKNOWLEDGEMENT', 'LEAVE_STATUS', 'TRIP_STATUS', 'LMS_REMINDER', 'LIBRARY_OVERDUE', 'SECURITY_ALERT', 'GENERAL');
DELETE FROM ref.lms_course_types WHERE code IN ('INTRODUCTORY', 'MANDATORY_POSITION', 'MANDATORY_DEPARTMENT', 'COMPLIANCE', 'OPTIONAL');
