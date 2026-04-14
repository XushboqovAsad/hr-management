
CREATE SCHEMA IF NOT EXISTS ref;

CREATE TABLE IF NOT EXISTS ref.order_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_order_types_code
    ON ref.order_types (lower(code))
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_ref_order_types_name
    ON ref.order_types (lower(name))
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS ref.leave_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_paid BOOLEAN NOT NULL DEFAULT TRUE,
    requires_document BOOLEAN NOT NULL DEFAULT FALSE,
    available_for_self_service BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_leave_types_code
    ON ref.leave_types (lower(code))
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_ref_leave_types_name
    ON ref.leave_types (lower(name))
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS ref.disciplinary_action_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    severity_rank INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_ref_disciplinary_action_types_severity_rank CHECK (severity_rank > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_disciplinary_action_types_code
    ON ref.disciplinary_action_types (lower(code))
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS ref.reward_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_monetary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_reward_types_code
    ON ref.reward_types (lower(code))
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS ref.document_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    confidential_by_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_document_types_code
    ON ref.document_types (lower(code))
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_ref_document_types_name
    ON ref.document_types (lower(name))
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS ref.notification_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_channel VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_ref_notification_types_default_channel CHECK (
        default_channel IN ('IN_APP', 'EMAIL', 'SMS', 'TELEGRAM', 'SYSTEM')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_notification_types_code
    ON ref.notification_types (lower(code))
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS ref.lms_course_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    mandatory_by_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ref_lms_course_types_code
    ON ref.lms_course_types (lower(code))
    WHERE is_deleted = FALSE;
