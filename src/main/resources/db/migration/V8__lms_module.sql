CREATE TABLE IF NOT EXISTS hr.lms_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(100),
    course_level VARCHAR(30) NOT NULL DEFAULT 'BASIC',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    mandatory_for_all BOOLEAN NOT NULL DEFAULT FALSE,
    introductory_course BOOLEAN NOT NULL DEFAULT FALSE,
    estimated_minutes INTEGER NOT NULL DEFAULT 0,
    certificate_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    certificate_template_code VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_courses_level CHECK (course_level IN ('INTRODUCTORY', 'BASIC', 'ADVANCED')),
    CONSTRAINT chk_lms_courses_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT chk_lms_courses_estimated_minutes CHECK (estimated_minutes >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_courses_code
    ON hr.lms_courses (lower(code))
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_lms_courses_search
    ON hr.lms_courses (lower(title), lower(COALESCE(category, '')));

CREATE TABLE IF NOT EXISTS hr.lms_course_modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES hr.lms_courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    module_order INTEGER NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_course_modules_order CHECK (module_order > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_course_modules_order
    ON hr.lms_course_modules (course_id, module_order)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_module_id UUID NOT NULL REFERENCES hr.lms_course_modules(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    lesson_order INTEGER NOT NULL,
    content_type VARCHAR(20) NOT NULL,
    content_url VARCHAR(1000),
    content_text TEXT,
    storage_key VARCHAR(500),
    mime_type VARCHAR(150),
    duration_minutes INTEGER NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_lessons_order CHECK (lesson_order > 0),
    CONSTRAINT chk_lms_lessons_content_type CHECK (content_type IN ('VIDEO', 'PDF', 'TEXT', 'LINK', 'TEST')),
    CONSTRAINT chk_lms_lessons_duration CHECK (duration_minutes >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_lessons_order
    ON hr.lms_lessons (course_module_id, lesson_order)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_tests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES hr.lms_lessons(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    pass_score NUMERIC(5,2) NOT NULL,
    attempt_limit INTEGER NOT NULL DEFAULT 3,
    randomize_questions BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_tests_pass_score CHECK (pass_score >= 0 AND pass_score <= 100),
    CONSTRAINT chk_lms_tests_attempt_limit CHECK (attempt_limit > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_tests_lesson
    ON hr.lms_tests (lesson_id)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_test_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_id UUID NOT NULL REFERENCES hr.lms_tests(id) ON DELETE CASCADE,
    question_order INTEGER NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    question_text VARCHAR(2000) NOT NULL,
    points NUMERIC(8,2) NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_test_questions_order CHECK (question_order > 0),
    CONSTRAINT chk_lms_test_questions_type CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT')),
    CONSTRAINT chk_lms_test_questions_points CHECK (points > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_test_questions_order
    ON hr.lms_test_questions (test_id, question_order)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_test_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES hr.lms_test_questions(id) ON DELETE CASCADE,
    option_order INTEGER NOT NULL,
    option_text VARCHAR(1000) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_test_options_order CHECK (option_order > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_test_options_order
    ON hr.lms_test_options (question_id, option_order)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_course_requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES hr.lms_courses(id) ON DELETE CASCADE,
    scope_type VARCHAR(20) NOT NULL,
    position_id UUID REFERENCES hr.positions(id),
    department_id UUID REFERENCES hr.departments(id),
    due_days INTEGER NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_course_requirements_scope CHECK (scope_type IN ('INTRODUCTORY', 'POSITION', 'DEPARTMENT', 'GLOBAL')),
    CONSTRAINT chk_lms_course_requirements_due_days CHECK (due_days >= 0)
);

CREATE INDEX IF NOT EXISTS ix_lms_course_requirements_position
    ON hr.lms_course_requirements (position_id, is_active)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS ix_lms_course_requirements_department
    ON hr.lms_course_requirements (department_id, is_active)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_course_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    course_id UUID NOT NULL REFERENCES hr.lms_courses(id),
    current_department_id UUID REFERENCES hr.departments(id),
    current_position_id UUID REFERENCES hr.positions(id),
    assigned_by_user_id UUID REFERENCES auth.users(id),
    assignment_source VARCHAR(20) NOT NULL,
    due_date DATE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    last_reminder_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_course_assignments_source CHECK (assignment_source IN ('HIRE', 'POSITION', 'DEPARTMENT', 'GLOBAL', 'MANUAL', 'INTRODUCTORY')),
    CONSTRAINT chk_lms_course_assignments_status CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'OVERDUE', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS ix_lms_course_assignments_employee
    ON hr.lms_course_assignments (employee_id, status, due_date);

CREATE INDEX IF NOT EXISTS ix_lms_course_assignments_course
    ON hr.lms_course_assignments (course_id, status);

CREATE TABLE IF NOT EXISTS hr.lms_lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES hr.lms_course_assignments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES hr.lms_lessons(id),
    progress_percent NUMERIC(5,2) NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    last_accessed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_lesson_progress_percent CHECK (progress_percent >= 0 AND progress_percent <= 100)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_lesson_progress_assignment_lesson
    ON hr.lms_lesson_progress (assignment_id, lesson_id)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_test_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES hr.lms_course_assignments(id) ON DELETE CASCADE,
    test_id UUID NOT NULL REFERENCES hr.lms_tests(id) ON DELETE CASCADE,
    attempt_no INTEGER NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    submitted_at TIMESTAMPTZ,
    score NUMERIC(5,2),
    passed BOOLEAN,
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_test_attempts_no CHECK (attempt_no > 0),
    CONSTRAINT chk_lms_test_attempts_score CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    CONSTRAINT chk_lms_test_attempts_status CHECK (status IN ('STARTED', 'SUBMITTED', 'AUTO_FAILED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_test_attempts_assignment_test_no
    ON hr.lms_test_attempts (assignment_id, test_id, attempt_no)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_test_attempt_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES hr.lms_test_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES hr.lms_test_questions(id),
    selected_option_id UUID REFERENCES hr.lms_test_options(id),
    free_text_answer VARCHAR(2000),
    is_correct BOOLEAN,
    points_awarded NUMERIC(8,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS ix_lms_test_attempt_answers_attempt
    ON hr.lms_test_attempt_answers (attempt_id)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL REFERENCES hr.lms_course_assignments(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    course_id UUID NOT NULL REFERENCES hr.lms_courses(id),
    certificate_number VARCHAR(100) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    mime_type VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lms_certificates_status CHECK (status IN ('ACTIVE', 'REVOKED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_certificates_assignment
    ON hr.lms_certificates (assignment_id)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_lms_certificates_number
    ON hr.lms_certificates (certificate_number)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS hr.lms_learning_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES hr.employees(id),
    assignment_id UUID REFERENCES hr.lms_course_assignments(id) ON DELETE SET NULL,
    course_id UUID REFERENCES hr.lms_courses(id) ON DELETE SET NULL,
    action_type VARCHAR(40) NOT NULL,
    details_json JSONB,
    action_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID REFERENCES auth.users(id),
    updated_by UUID REFERENCES auth.users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS ix_lms_learning_history_employee
    ON hr.lms_learning_history (employee_id, action_at DESC)
    WHERE is_deleted = FALSE;
