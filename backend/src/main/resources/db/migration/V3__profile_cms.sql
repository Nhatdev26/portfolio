CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    display_name VARCHAR(160) NOT NULL,
    email VARCHAR(320) NOT NULL,
    location VARCHAR(160),
    primary_role VARCHAR(160) NOT NULL,
    career_direction VARCHAR(255),
    main_tech_focus VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_profiles_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE')),
    CONSTRAINT fk_profiles_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_profiles_one_active ON profiles (status)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;
CREATE INDEX ix_profiles_status ON profiles (status);

CREATE TABLE profile_contents (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    language VARCHAR(8) NOT NULL,
    headline VARCHAR(255) NOT NULL,
    subheadline VARCHAR(255),
    short_bio TEXT,
    long_bio TEXT,
    seo_title VARCHAR(255),
    seo_description VARCHAR(320),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT fk_profile_contents_profile FOREIGN KEY (profile_id) REFERENCES profiles (id),
    CONSTRAINT fk_profile_contents_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT ck_profile_contents_language CHECK (language IN ('EN', 'VI')),
    CONSTRAINT ck_profile_contents_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX ux_profile_contents_active_language
    ON profile_contents (profile_id, language)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;
CREATE INDEX ix_profile_contents_profile ON profile_contents (profile_id);
CREATE INDEX ix_profile_contents_status ON profile_contents (status);

CREATE TABLE social_links (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    platform VARCHAR(32) NOT NULL,
    label VARCHAR(120) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT fk_social_links_profile FOREIGN KEY (profile_id) REFERENCES profiles (id),
    CONSTRAINT fk_social_links_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id),
    CONSTRAINT ck_social_links_platform CHECK (platform IN ('GITHUB', 'LINKEDIN', 'EMAIL', 'PORTFOLIO', 'OTHER')),
    CONSTRAINT ck_social_links_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX ix_social_links_profile ON social_links (profile_id);
CREATE INDEX ix_social_links_status ON social_links (status);
CREATE INDEX ix_social_links_order ON social_links (display_order);
