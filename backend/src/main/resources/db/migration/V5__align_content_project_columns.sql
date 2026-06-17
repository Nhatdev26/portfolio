ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS problem_statement TEXT,
    ADD COLUMN IF NOT EXISTS solution_overview TEXT,
    ADD COLUMN IF NOT EXISTS frontend_highlights TEXT,
    ADD COLUMN IF NOT EXISTS source_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS demo_url VARCHAR(500);
