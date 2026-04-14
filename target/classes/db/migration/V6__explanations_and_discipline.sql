CREATE TABLE IF NOT EXISTS hr.explanation_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    department_id UUID REFERENCES hr.departments(id),
    attendance_incident_id UUID REFERENCES hr.attendance_incidents(id),
    incident_source VARCHAR(30) NOT NULL,
    incident_type VARCHAR(40) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    occurred_at TIMESTAMPTZ NOT NULL,
    manager_employee_id UUID REFERENCES hr.employees(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_EXPLANATION',
    explanation_required BOOLEAN NOT NULL DEFAULT TRUE,
    due_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_explanation_incidents_source CHECK (incident_source IN ('SCUD', 'MANUAL', 'MANAGER')),
    CONSTRAINT chk_explanation_incidents_status CHECK (status IN ('OPEN', 'PENDING_EXPLANATION', 'UNDER_REVIEW', 'RESOLVED', 'WAIVED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_explanation_incidents_attendance_incident
    ON hr.explanation_incidents (attendance_incident_id)
    WHERE attendance_incident_id IS NOT NULL AND is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_explanation_incidents_employee_status
    ON hr.explanation_incidents (employee_id, status, due_at);

CREATE INDEX IF NOT EXISTS ix_explanation_incidents_department_occurred
    ON hr.explanation_incidents (department_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS hr.explanations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    explanation_incident_id UUID NOT NULL REFERENCES hr.explanation_incidents(id),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    explanation_text VARCHAR(4000),
    employee_submitted_at TIMESTAMPTZ,
    manager_reviewer_employee_id UUID REFERENCES hr.employees(id),
    manager_review_comment VARCHAR(2000),
    manager_reviewed_at TIMESTAMPTZ,
    hr_reviewer_employee_id UUID REFERENCES hr.employees(id),
    hr_decision_comment VARCHAR(2000),
    hr_decided_at TIMESTAMPTZ,
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    source_order_id UUID,
    source_order_number VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_explanations_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'MANAGER_REVIEWED', 'ACCEPTED', 'REJECTED', 'DISCIPLINARY_ACTION_CREATED', 'CLOSED_NO_CONSEQUENCE'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_explanations_incident
    ON hr.explanations (explanation_incident_id)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_explanations_employee_status
    ON hr.explanations (employee_id, status, created_at DESC);

CREATE TABLE IF NOT EXISTS hr.explanation_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    explanation_id UUID NOT NULL REFERENCES hr.explanations(id) ON DELETE CASCADE,
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
    CONSTRAINT chk_explanation_documents_size CHECK (size_bytes >= 0),
    CONSTRAINT chk_explanation_documents_version CHECK (version_no > 0)
);

CREATE INDEX IF NOT EXISTS ix_explanation_documents_explanation
    ON hr.explanation_documents (explanation_id, created_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS ux_explanation_documents_current_title
    ON hr.explanation_documents (explanation_id, lower(title))
    WHERE is_current = TRUE AND is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.explanation_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    explanation_incident_id UUID NOT NULL REFERENCES hr.explanation_incidents(id) ON DELETE CASCADE,
    explanation_id UUID REFERENCES hr.explanations(id),
    action_type VARCHAR(50) NOT NULL,
    status_from VARCHAR(40),
    status_to VARCHAR(40),
    actor_user_id UUID REFERENCES auth.users(id),
    comment_text VARCHAR(2000),
    payload_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS ix_explanation_history_incident
    ON hr.explanation_history (explanation_incident_id, created_at DESC);

CREATE TABLE IF NOT EXISTS hr.disciplinary_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    department_id UUID REFERENCES hr.departments(id),
    explanation_incident_id UUID REFERENCES hr.explanation_incidents(id),
    explanation_id UUID REFERENCES hr.explanations(id),
    action_type VARCHAR(30) NOT NULL,
    action_date DATE NOT NULL,
    reason_text VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    valid_until DATE,
    source_order_id UUID,
    source_order_number VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_disciplinary_actions_type CHECK (action_type IN ('REMARK', 'REPRIMAND', 'SEVERE_REPRIMAND')),
    CONSTRAINT chk_disciplinary_actions_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'CLOSED')),
    CONSTRAINT chk_disciplinary_actions_dates CHECK (valid_until IS NULL OR valid_until >= action_date)
);

CREATE INDEX IF NOT EXISTS ix_disciplinary_actions_employee_date
    ON hr.disciplinary_actions (employee_id, action_date DESC);

CREATE INDEX IF NOT EXISTS ix_disciplinary_actions_department_date
    ON hr.disciplinary_actions (department_id, action_date DESC);

CREATE TABLE IF NOT EXISTS hr.reward_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    department_id UUID REFERENCES hr.departments(id),
    reward_type VARCHAR(30) NOT NULL,
    reward_date DATE NOT NULL,
    amount NUMERIC(18, 2),
    currency_code VARCHAR(3) DEFAULT 'UZS',
    reason_text VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    source_order_id UUID,
    source_order_number VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_reward_actions_type CHECK (reward_type IN ('THANKS', 'AWARD', 'CERTIFICATE', 'BONUS')),
    CONSTRAINT chk_reward_actions_status CHECK (status IN ('DRAFT', 'APPROVED', 'GRANTED', 'CANCELLED')),
    CONSTRAINT chk_reward_actions_amount CHECK (amount IS NULL OR amount >= 0)
);

CREATE INDEX IF NOT EXISTS ix_reward_actions_employee_date
    ON hr.reward_actions (employee_id, reward_date DESC);

CREATE INDEX IF NOT EXISTS ix_reward_actions_department_date
    ON hr.reward_actions (department_id, reward_date DESC);

CREATE TABLE IF NOT EXISTS hr.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_employee_id UUID REFERENCES hr.employees(id),
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    read_at TIMESTAMPTZ,
    payload_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_notifications_status CHECK (status IN ('NEW', 'READ', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS ix_notifications_recipient_status
    ON hr.notifications (recipient_employee_id, status, created_at DESC);
