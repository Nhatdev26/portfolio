CREATE TABLE cv_files (
    id BIGSERIAL PRIMARY KEY,
    language VARCHAR(8) NOT NULL,
    target_role VARCHAR(120) NOT NULL,
    version VARCHAR(80) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    file_data BYTEA NOT NULL,
    status VARCHAR(32) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    activated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_cv_files_language CHECK (language IN ('EN', 'VI')),
    CONSTRAINT ck_cv_files_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX ux_cv_files_active_language_role
    ON cv_files (language, target_role)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE INDEX ix_cv_files_status ON cv_files (status);
CREATE INDEX ix_cv_files_language_role ON cv_files (language, target_role);
