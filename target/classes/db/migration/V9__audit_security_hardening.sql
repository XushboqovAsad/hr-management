CREATE TABLE IF NOT EXISTS auth.login_audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES auth.users(id),
    username VARCHAR(100),
    event_type VARCHAR(20) NOT NULL,
    result VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    ip_address VARCHAR(100),
    user_agent TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_login_audit_event_type CHECK (event_type IN ('LOGIN', 'REFRESH', 'LOGOUT')),
    CONSTRAINT chk_login_audit_result CHECK (result IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX IF NOT EXISTS ix_login_audit_logs_occurred_at
    ON auth.login_audit_logs (occurred_at DESC);

CREATE INDEX IF NOT EXISTS ix_login_audit_logs_username
    ON auth.login_audit_logs (lower(username), occurred_at DESC);

CREATE TABLE IF NOT EXISTS audit.personal_data_access_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_employee_id UUID,
    target_employee_id UUID NOT NULL,
    request_uri VARCHAR(500),
    access_type VARCHAR(30) NOT NULL,
    fields_accessed TEXT,
    masked_fields TEXT,
    access_allowed BOOLEAN NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_personal_data_access_type CHECK (access_type IN ('READ', 'EXPORT', 'UPDATE'))
);

CREATE INDEX IF NOT EXISTS ix_personal_data_access_logs_target
    ON audit.personal_data_access_logs (target_employee_id, occurred_at DESC);

CREATE INDEX IF NOT EXISTS ix_personal_data_access_logs_actor
    ON audit.personal_data_access_logs (actor_user_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS audit.document_access_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_employee_id UUID,
    document_module VARCHAR(100) NOT NULL,
    entity_id UUID,
    document_id UUID NOT NULL,
    storage_key VARCHAR(500),
    access_mode VARCHAR(20) NOT NULL,
    request_uri VARCHAR(500),
    access_allowed BOOLEAN NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_document_access_mode CHECK (access_mode IN ('DOWNLOAD', 'PREVIEW'))
);

CREATE INDEX IF NOT EXISTS ix_document_access_logs_document
    ON audit.document_access_logs (document_module, document_id, occurred_at DESC);

CREATE INDEX IF NOT EXISTS ix_document_access_logs_actor
    ON audit.document_access_logs (actor_user_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS audit.admin_action_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_employee_id UUID,
    request_method VARCHAR(10) NOT NULL,
    request_uri VARCHAR(500) NOT NULL,
    status_code INTEGER NOT NULL,
    details_json TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_admin_action_logs_occurred_at
    ON audit.admin_action_logs (occurred_at DESC);

CREATE INDEX IF NOT EXISTS ix_admin_action_logs_actor
    ON audit.admin_action_logs (actor_user_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS audit.hr_decision_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_employee_id UUID,
    module_code VARCHAR(100) NOT NULL,
    decision_action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    request_uri VARCHAR(500) NOT NULL,
    status_code INTEGER NOT NULL,
    details_json TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_hr_decision_logs_occurred_at
    ON audit.hr_decision_logs (occurred_at DESC);

CREATE INDEX IF NOT EXISTS ix_hr_decision_logs_module
    ON audit.hr_decision_logs (module_code, occurred_at DESC);
