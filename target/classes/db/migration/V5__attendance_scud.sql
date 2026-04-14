CREATE TABLE IF NOT EXISTS hr.work_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    shift_start_time TIME NOT NULL,
    shift_end_time TIME NOT NULL,
    crosses_midnight BOOLEAN NOT NULL DEFAULT FALSE,
    grace_minutes INTEGER NOT NULL DEFAULT 10,
    required_minutes INTEGER NOT NULL DEFAULT 480,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_work_schedules_minutes CHECK (grace_minutes >= 0 AND required_minutes > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_work_schedules_code
    ON hr.work_schedules (code)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.employee_work_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    work_schedule_id UUID NOT NULL REFERENCES hr.work_schedules(id),
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_employee_work_schedule_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX IF NOT EXISTS ix_employee_work_schedules_employee_dates
    ON hr.employee_work_schedules (employee_id, effective_from, effective_to);

CREATE TABLE IF NOT EXISTS hr.scud_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID REFERENCES hr.employees(id),
    source_system VARCHAR(50) NOT NULL DEFAULT 'SCUD',
    external_event_id VARCHAR(150),
    badge_number VARCHAR(100),
    device_id VARCHAR(100) NOT NULL,
    device_name VARCHAR(255),
    event_type VARCHAR(20) NOT NULL,
    event_at TIMESTAMPTZ NOT NULL,
    raw_payload JSONB,
    normalization_status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    processed_at TIMESTAMPTZ,
    error_message VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_scud_event_type CHECK (event_type IN ('ENTRY', 'EXIT')),
    CONSTRAINT chk_scud_normalization_status CHECK (normalization_status IN ('NEW', 'PROCESSED', 'IGNORED', 'ERROR'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_scud_events_external
    ON hr.scud_events (external_event_id)
    WHERE external_event_id IS NOT NULL AND is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_scud_events_employee_time
    ON hr.scud_events (employee_id, event_at);

CREATE TABLE IF NOT EXISTS hr.attendance_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    work_date DATE NOT NULL,
    work_schedule_id UUID REFERENCES hr.work_schedules(id),
    scheduled_start_at TIMESTAMPTZ,
    scheduled_end_at TIMESTAMPTZ,
    first_in_at TIMESTAMPTZ,
    last_out_at TIMESTAMPTZ,
    worked_minutes INTEGER NOT NULL DEFAULT 0,
    raw_event_count INTEGER NOT NULL DEFAULT 0,
    missing_in BOOLEAN NOT NULL DEFAULT FALSE,
    missing_out BOOLEAN NOT NULL DEFAULT FALSE,
    no_scud_data BOOLEAN NOT NULL DEFAULT FALSE,
    log_status VARCHAR(20) NOT NULL DEFAULT 'CALCULATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_attendance_logs_values CHECK (worked_minutes >= 0 AND raw_event_count >= 0),
    CONSTRAINT chk_attendance_logs_status CHECK (log_status IN ('CALCULATED', 'MANUAL', 'ERROR'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_attendance_logs_employee_date
    ON hr.attendance_logs (employee_id, work_date)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.attendance_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    employee_assignment_id UUID REFERENCES hr.employee_assignments(id),
    department_id UUID REFERENCES hr.departments(id),
    attendance_log_id UUID REFERENCES hr.attendance_logs(id),
    work_date DATE NOT NULL,
    work_schedule_id UUID REFERENCES hr.work_schedules(id),
    attendance_status VARCHAR(40) NOT NULL,
    late_minutes INTEGER NOT NULL DEFAULT 0,
    early_leave_minutes INTEGER NOT NULL DEFAULT 0,
    overtime_minutes INTEGER NOT NULL DEFAULT 0,
    absence_minutes INTEGER NOT NULL DEFAULT 0,
    violation_count INTEGER NOT NULL DEFAULT 0,
    incident_created BOOLEAN NOT NULL DEFAULT FALSE,
    manually_adjusted BOOLEAN NOT NULL DEFAULT FALSE,
    adjusted_comment VARCHAR(1000),
    finalized_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_attendance_summary_minutes CHECK (late_minutes >= 0 AND early_leave_minutes >= 0 AND overtime_minutes >= 0 AND absence_minutes >= 0 AND violation_count >= 0),
    CONSTRAINT chk_attendance_summary_status CHECK (attendance_status IN ('PRESENT', 'LATE', 'EARLY_LEAVE', 'MISSING_PUNCH', 'ABSENT', 'NO_DATA', 'OVERTIME', 'REMOTE_WORK', 'SICK_LEAVE', 'BUSINESS_TRIP', 'UNPAID_LEAVE', 'EXCUSED_ABSENCE', 'ABSENCE_UNEXCUSED', 'DOWNTIME', 'OTHER', 'MANUAL'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_attendance_summaries_employee_date
    ON hr.attendance_summaries (employee_id, work_date)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_attendance_summaries_filters
    ON hr.attendance_summaries (work_date, department_id, employee_id, attendance_status);

CREATE TABLE IF NOT EXISTS hr.attendance_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_summary_id UUID NOT NULL REFERENCES hr.attendance_summaries(id) ON DELETE CASCADE,
    adjusted_start_at TIMESTAMPTZ,
    adjusted_end_at TIMESTAMPTZ,
    adjusted_status VARCHAR(40) NOT NULL,
    adjusted_reason VARCHAR(1000) NOT NULL,
    approved_by UUID REFERENCES auth.users(id),
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS ix_attendance_adjustments_summary
    ON hr.attendance_adjustments (attendance_summary_id, created_at DESC);

CREATE TABLE IF NOT EXISTS hr.attendance_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    attendance_summary_id UUID NOT NULL REFERENCES hr.attendance_summaries(id),
    incident_type VARCHAR(40) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    explanation_required BOOLEAN NOT NULL DEFAULT TRUE,
    due_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_attendance_incident_status CHECK (status IN ('OPEN', 'PENDING_EXPLANATION', 'UNDER_REVIEW', 'RESOLVED', 'WAIVED'))
);

CREATE INDEX IF NOT EXISTS ix_attendance_incidents_employee
    ON hr.attendance_incidents (employee_id, status, due_at);

CREATE TABLE IF NOT EXISTS hr.lateness_violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    attendance_summary_id UUID NOT NULL REFERENCES hr.attendance_summaries(id),
    attendance_incident_id UUID REFERENCES hr.attendance_incidents(id),
    violation_type VARCHAR(40) NOT NULL,
    scheduled_at TIMESTAMPTZ,
    actual_at TIMESTAMPTZ,
    minutes_value INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lateness_violation_type CHECK (violation_type IN ('LATENESS', 'EARLY_LEAVE', 'MISSING_PUNCH', 'ABSENCE', 'NO_DATA', 'OVERTIME')),
    CONSTRAINT chk_lateness_violation_status CHECK (status IN ('OPEN', 'EXPLANATION_REQUESTED', 'EXPLAINED', 'WAIVED', 'CLOSED')),
    CONSTRAINT chk_lateness_violation_minutes CHECK (minutes_value >= 0)
);

CREATE INDEX IF NOT EXISTS ix_lateness_violations_filter
    ON hr.lateness_violations (employee_id, violation_type, status, created_at DESC);

INSERT INTO hr.work_schedules (code, name, shift_start_time, shift_end_time, crosses_midnight, grace_minutes, required_minutes)
SELECT 'STANDARD_0900_1800', 'Standard 09:00-18:00', TIME '09:00', TIME '18:00', FALSE, 10, 480
WHERE NOT EXISTS (SELECT 1 FROM hr.work_schedules WHERE code = 'STANDARD_0900_1800' AND is_deleted = FALSE);

INSERT INTO hr.work_schedules (code, name, shift_start_time, shift_end_time, crosses_midnight, grace_minutes, required_minutes)
SELECT 'NIGHT_2200_0600', 'Night 22:00-06:00', TIME '22:00', TIME '06:00', TRUE, 10, 480
WHERE NOT EXISTS (SELECT 1 FROM hr.work_schedules WHERE code = 'NIGHT_2200_0600' AND is_deleted = FALSE);
