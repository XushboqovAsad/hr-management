CREATE TABLE IF NOT EXISTS hr.dismissal_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    initiator_employee_id UUID REFERENCES hr.employees(id),
    initiator_user_id UUID REFERENCES auth.users(id),
    department_id UUID REFERENCES hr.departments(id),
    reason_type VARCHAR(40) NOT NULL,
    reason_text VARCHAR(2000) NOT NULL,
    dismissal_date DATE NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    order_number VARCHAR(100),
    order_template_code VARCHAR(100),
    order_generated_at TIMESTAMPTZ,
    order_print_form_html TEXT,
    approved_at TIMESTAMPTZ,
    finalized_at TIMESTAMPTZ,
    archived_at TIMESTAMPTZ,
    account_blocked_at TIMESTAMPTZ,
    final_payroll_sync_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    comment_text VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_dismissal_requests_reason_type CHECK (
        reason_type IN ('RESIGNATION', 'MUTUAL_AGREEMENT', 'CONTRACT_EXPIRY', 'REDUCTION', 'DISCIPLINARY', 'OTHER')
    ),
    CONSTRAINT chk_dismissal_requests_status CHECK (
        status IN ('DRAFT', 'ON_APPROVAL', 'APPROVED', 'REJECTED', 'ORDER_CREATED', 'CLEARANCE_IN_PROGRESS', 'READY_FOR_FINALIZATION', 'FINALIZED', 'ARCHIVED', 'CANCELLED')
    ),
    CONSTRAINT chk_dismissal_requests_payroll_sync_status CHECK (
        final_payroll_sync_status IN ('PENDING', 'SENT', 'ACKNOWLEDGED', 'FAILED')
    )
);

CREATE INDEX IF NOT EXISTS ix_dismissal_requests_employee_status
    ON hr.dismissal_requests (employee_id, status, dismissal_date DESC);

CREATE INDEX IF NOT EXISTS ix_dismissal_requests_department_status
    ON hr.dismissal_requests (department_id, status, dismissal_date DESC);

CREATE TABLE IF NOT EXISTS hr.clearance_checklists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dismissal_request_id UUID NOT NULL REFERENCES hr.dismissal_requests(id) ON DELETE CASCADE,
    checklist_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_clearance_checklists_status CHECK (
        checklist_status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_clearance_checklists_request
    ON hr.clearance_checklists (dismissal_request_id)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.clearance_checklist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clearance_checklist_id UUID NOT NULL REFERENCES hr.clearance_checklists(id) ON DELETE CASCADE,
    item_type VARCHAR(40) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    item_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    return_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    responsible_role VARCHAR(50),
    responsible_user_id UUID REFERENCES auth.users(id),
    due_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    asset_code VARCHAR(100),
    asset_name VARCHAR(255),
    comment_text VARCHAR(2000),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_clearance_checklist_items_type CHECK (
        item_type IN ('PASS', 'LAPTOP', 'PHONE', 'SIM_CARD', 'DOCUMENTS', 'BOOKS', 'OTHER_ASSET', 'ACTIVE_TASKS', 'FINAL_PAYROLL', 'LMS_ACCESS', 'ACCOUNT_BLOCK')
    ),
    CONSTRAINT chk_clearance_checklist_items_status CHECK (
        item_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'WAIVED')
    ),
    CONSTRAINT chk_clearance_checklist_items_return_status CHECK (
        return_status IN ('PENDING', 'RETURNED', 'WAIVED', 'NOT_REQUIRED')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_clearance_checklist_items_unique
    ON hr.clearance_checklist_items (clearance_checklist_id, item_type)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_clearance_checklist_items_checklist
    ON hr.clearance_checklist_items (clearance_checklist_id, sort_order);

CREATE TABLE IF NOT EXISTS hr.dismissal_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dismissal_request_id UUID NOT NULL REFERENCES hr.dismissal_requests(id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS ix_dismissal_history_request
    ON hr.dismissal_history (dismissal_request_id, created_at DESC);
