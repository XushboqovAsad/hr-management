CREATE TABLE IF NOT EXISTS hr.absence_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    requester_employee_id UUID REFERENCES hr.employees(id),
    absence_type VARCHAR(40) NOT NULL,
    reason_text VARCHAR(2000),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    document_required BOOLEAN NOT NULL DEFAULT FALSE,
    hr_comment VARCHAR(2000),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    payroll_sync_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_absence_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_absence_type CHECK (absence_type IN ('SICK_LEAVE', 'EXCUSED_ABSENCE', 'ABSENCE_UNEXCUSED', 'UNPAID_LEAVE', 'REMOTE_WORK', 'DOWNTIME', 'OTHER')),
    CONSTRAINT chk_absence_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'HR_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED', 'CLOSED')),
    CONSTRAINT chk_absence_payroll_sync CHECK (payroll_sync_status IN ('PENDING', 'SENT', 'ACKNOWLEDGED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS ix_absence_records_employee_dates
    ON hr.absence_records (employee_id, start_date, end_date);

CREATE INDEX IF NOT EXISTS ix_absence_records_status
    ON hr.absence_records (status, absence_type);

CREATE TABLE IF NOT EXISTS hr.absence_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    absence_record_id UUID NOT NULL REFERENCES hr.absence_records(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    version_no INTEGER NOT NULL DEFAULT 1,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    document_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_absence_document_status CHECK (document_status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_absence_document_size CHECK (size_bytes > 0)
);

CREATE INDEX IF NOT EXISTS ix_absence_documents_record
    ON hr.absence_documents (absence_record_id, is_current, is_deleted);

CREATE TABLE IF NOT EXISTS hr.absence_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    absence_record_id UUID NOT NULL REFERENCES hr.absence_records(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL,
    status_from VARCHAR(30),
    status_to VARCHAR(30),
    actor_user_id UUID REFERENCES auth.users(id),
    comment_text VARCHAR(1000),
    payload_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS ix_absence_history_record
    ON hr.absence_history (absence_record_id, created_at DESC);

CREATE TABLE IF NOT EXISTS hr.attendance_day_marks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    attendance_date DATE NOT NULL,
    mark_source VARCHAR(30) NOT NULL,
    source_record_id UUID NOT NULL,
    mark_status VARCHAR(40) NOT NULL,
    note_text VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_attendance_day_marks_source CHECK (mark_source IN ('ABSENCE', 'LEAVE', 'BUSINESS_TRIP', 'MANUAL'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_attendance_day_marks_unique
    ON hr.attendance_day_marks (employee_id, attendance_date, mark_source, source_record_id)
    WHERE is_deleted = FALSE;
