CREATE TABLE IF NOT EXISTS hr.business_trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    requester_employee_id UUID REFERENCES hr.employees(id),
    approver_department_id UUID REFERENCES hr.departments(id),
    destination_country VARCHAR(100),
    destination_city VARCHAR(150) NOT NULL,
    destination_address VARCHAR(255),
    purpose VARCHAR(1000) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    transport_type VARCHAR(100),
    accommodation_details VARCHAR(1000),
    daily_allowance NUMERIC(18,2) NOT NULL DEFAULT 0,
    funding_source VARCHAR(255),
    comment_text VARCHAR(2000),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    order_number VARCHAR(100),
    order_template_code VARCHAR(100),
    order_generated_at TIMESTAMPTZ,
    order_print_form_html TEXT,
    order_pdf_document_id UUID,
    report_text TEXT,
    report_submitted_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    payroll_sync_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_business_trip_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_business_trip_daily_allowance CHECK (daily_allowance >= 0),
    CONSTRAINT chk_business_trip_status CHECK (status IN ('DRAFT', 'ON_APPROVAL', 'APPROVED', 'REJECTED', 'ORDER_CREATED', 'IN_PROGRESS', 'REPORT_PENDING', 'REPORT_SUBMITTED', 'CLOSED', 'CANCELLED', 'OVERDUE')),
    CONSTRAINT chk_business_trip_payroll_status CHECK (payroll_sync_status IN ('PENDING', 'SENT', 'ACKNOWLEDGED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS ix_business_trips_employee_status
    ON hr.business_trips (employee_id, status, start_date);

CREATE INDEX IF NOT EXISTS ix_business_trips_dates
    ON hr.business_trips (start_date, end_date);

CREATE TABLE IF NOT EXISTS hr.business_trip_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_trip_id UUID NOT NULL REFERENCES hr.business_trips(id) ON DELETE CASCADE,
    document_kind VARCHAR(40) NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    version_no INTEGER NOT NULL DEFAULT 1,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_business_trip_document_kind CHECK (document_kind IN ('REQUEST_ATTACHMENT', 'ORDER_ATTACHMENT', 'REPORT_ATTACHMENT', 'CONFIRMING_DOCUMENT')),
    CONSTRAINT chk_business_trip_document_size CHECK (size_bytes > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_business_trip_documents_current
    ON hr.business_trip_documents (business_trip_id, document_kind, title)
    WHERE is_current = TRUE AND is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_business_trip_documents_trip
    ON hr.business_trip_documents (business_trip_id, document_kind, is_current);

CREATE TABLE IF NOT EXISTS hr.business_trip_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_trip_id UUID NOT NULL REFERENCES hr.business_trips(id) ON DELETE CASCADE,
    step_no INTEGER NOT NULL,
    approval_role VARCHAR(50) NOT NULL,
    approver_user_id UUID REFERENCES auth.users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    decision_comment VARCHAR(1000),
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_business_trip_approval_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SKIPPED')),
    CONSTRAINT chk_business_trip_approval_role CHECK (approval_role IN ('MANAGER', 'HR_ADMIN', 'HR_INSPECTOR'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_business_trip_approval_step
    ON hr.business_trip_approvals (business_trip_id, step_no)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.business_trip_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_trip_id UUID NOT NULL REFERENCES hr.business_trips(id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS ix_business_trip_history_trip
    ON hr.business_trip_history (business_trip_id, created_at DESC);
