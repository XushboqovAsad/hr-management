ALTER TABLE hr.employees
    ADD COLUMN IF NOT EXISTS hire_date DATE;

ALTER TABLE hr.employees
    ADD COLUMN IF NOT EXISTS dismissal_date DATE;

UPDATE hr.employees
SET hire_date = COALESCE(hire_date, created_at::date)
WHERE hire_date IS NULL;

ALTER TABLE hr.employees
    ALTER COLUMN hire_date SET NOT NULL;
