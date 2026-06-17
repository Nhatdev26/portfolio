CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'EDITOR', 'REVIEWER', 'VIEWER')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED')),
    CONSTRAINT fk_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_users_email_active ON users (LOWER(email)) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_status ON users (status);
CREATE INDEX ix_users_role ON users (role);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_ip VARCHAR(64),
    revoked_by_ip VARCHAR(64),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ux_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX ix_refresh_tokens_revoked_at ON refresh_tokens (revoked_at);

