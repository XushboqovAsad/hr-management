CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS hr;
CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash TEXT NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_users_username ON auth.users (lower(username)) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_users_email ON auth.users (lower(email)) WHERE email IS NOT NULL AND is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS auth.roles (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_roles_code ON auth.roles (code) WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS auth.permissions (
    id UUID PRIMARY KEY,
    module_code VARCHAR(100) NOT NULL,
    action_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_permissions_module_action ON auth.permissions (module_code, action_code) WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.departments (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_department_id UUID REFERENCES hr.departments(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_departments_code ON hr.departments (code) WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.employees (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id),
    personnel_number VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_employees_personnel_number ON hr.employees (personnel_number) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_employees_user_id ON hr.employees (user_id) WHERE user_id IS NOT NULL AND is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.employee_assignments (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    department_id UUID NOT NULL REFERENCES hr.departments(id),
    manager_employee_id UUID REFERENCES hr.employees(id),
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_employee_assignments_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS ix_hr_employee_assignments_employee ON hr.employee_assignments (employee_id, effective_from DESC);
CREATE INDEX IF NOT EXISTS ix_hr_employee_assignments_manager ON hr.employee_assignments (manager_employee_id, effective_from DESC);

CREATE TABLE IF NOT EXISTS auth.user_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id),
    role_id UUID NOT NULL REFERENCES auth.roles(id),
    scope_type VARCHAR(30) NOT NULL,
    scope_department_id UUID REFERENCES hr.departments(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE NOT NULL,
    valid_to DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_user_roles_dates CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX IF NOT EXISTS ix_auth_user_roles_user ON auth.user_roles (user_id, is_active);

CREATE TABLE IF NOT EXISTS auth.role_permissions (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES auth.roles(id),
    permission_id UUID NOT NULL REFERENCES auth.permissions(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_role_permissions_unique ON auth.role_permissions (role_id, permission_id) WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id),
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    user_agent TEXT,
    ip_address VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_auth_refresh_tokens_hash ON auth.refresh_tokens (token_hash);

CREATE TABLE IF NOT EXISTS auth.access_delegations (
    id UUID PRIMARY KEY,
    grantor_user_id UUID NOT NULL REFERENCES auth.users(id),
    grantee_user_id UUID NOT NULL REFERENCES auth.users(id),
    module_code VARCHAR(100) NOT NULL,
    action_code VARCHAR(100) NOT NULL,
    scope_type VARCHAR(30) NOT NULL,
    scope_department_id UUID REFERENCES hr.departments(id),
    employee_id UUID REFERENCES hr.employees(id),
    valid_from DATE NOT NULL,
    valid_to DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_access_delegations_dates CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX IF NOT EXISTS ix_auth_access_delegations_grantee ON auth.access_delegations (grantee_user_id, module_code, action_code);

CREATE TABLE IF NOT EXISTS audit.audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_employee_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_schema VARCHAR(50),
    entity_table VARCHAR(100),
    entity_id UUID,
    details_json TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS ix_audit_logs_occurred_at ON audit.audit_logs (occurred_at DESC);
