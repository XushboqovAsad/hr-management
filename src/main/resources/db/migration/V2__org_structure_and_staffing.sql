CREATE SCHEMA IF NOT EXISTS hr;

ALTER TABLE hr.departments
    ADD COLUMN IF NOT EXISTS unit_type VARCHAR(30);

ALTER TABLE hr.departments
    ADD COLUMN IF NOT EXISTS manager_employee_id UUID;

ALTER TABLE hr.departments
    ADD COLUMN IF NOT EXISTS phone VARCHAR(50);

ALTER TABLE hr.departments
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);

ALTER TABLE hr.departments
    ADD COLUMN IF NOT EXISTS location VARCHAR(255);

ALTER TABLE hr.departments
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMPTZ;

UPDATE hr.departments
SET unit_type = 'DEPARTMENT'
WHERE unit_type IS NULL;

ALTER TABLE hr.departments
    ALTER COLUMN unit_type SET NOT NULL;

CREATE TABLE IF NOT EXISTS hr.positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_positions_code
    ON hr.positions (lower(code))
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_hr_positions_title
    ON hr.positions (lower(title));

CREATE TABLE IF NOT EXISTS hr.staffing_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    department_id UUID NOT NULL REFERENCES hr.departments(id),
    position_id UUID NOT NULL REFERENCES hr.positions(id),
    planned_fte NUMERIC(10,2) NOT NULL,
    occupied_fte NUMERIC(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    opened_at DATE,
    closed_at DATE,
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_hr_staffing_units_fte CHECK (planned_fte > 0 AND occupied_fte >= 0 AND occupied_fte <= planned_fte),
    CONSTRAINT chk_hr_staffing_units_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    CONSTRAINT chk_hr_staffing_units_dates CHECK (closed_at IS NULL OR opened_at IS NULL OR closed_at >= opened_at)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_staffing_units_code
    ON hr.staffing_units (lower(code))
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_hr_staffing_units_department
    ON hr.staffing_units (department_id, status);

CREATE INDEX IF NOT EXISTS ix_hr_staffing_units_position
    ON hr.staffing_units (position_id, status);

CREATE TABLE IF NOT EXISTS hr.department_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_id UUID NOT NULL REFERENCES hr.departments(id),
    version_no INTEGER NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    unit_type VARCHAR(30) NOT NULL,
    parent_department_id UUID,
    manager_employee_id UUID,
    active BOOLEAN NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_department_history_version
    ON hr.department_history (department_id, version_no)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.position_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    position_id UUID NOT NULL REFERENCES hr.positions(id),
    version_no INTEGER NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    active BOOLEAN NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_position_history_version
    ON hr.position_history (position_id, version_no)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.staffing_unit_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staffing_unit_id UUID NOT NULL REFERENCES hr.staffing_units(id),
    version_no INTEGER NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    department_id UUID NOT NULL REFERENCES hr.departments(id),
    position_id UUID NOT NULL REFERENCES hr.positions(id),
    planned_fte NUMERIC(10,2) NOT NULL,
    occupied_fte NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    opened_at DATE,
    closed_at DATE,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_hr_staffing_unit_history_version
    ON hr.staffing_unit_history (staffing_unit_id, version_no)
    WHERE is_deleted = FALSE;

ALTER TABLE hr.employee_assignments
    ADD COLUMN IF NOT EXISTS position_id UUID REFERENCES hr.positions(id);

ALTER TABLE hr.employee_assignments
    ADD COLUMN IF NOT EXISTS staffing_unit_id UUID REFERENCES hr.staffing_units(id);

CREATE INDEX IF NOT EXISTS ix_hr_departments_parent
    ON hr.departments (parent_department_id)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_hr_departments_type
    ON hr.departments (unit_type)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_hr_employee_assignments_staffing_unit
    ON hr.employee_assignments (staffing_unit_id)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_hr_employee_assignments_position
    ON hr.employee_assignments (position_id)
    WHERE is_deleted = FALSE;
