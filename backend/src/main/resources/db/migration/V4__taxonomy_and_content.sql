CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_categories_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT fk_categories_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_categories_slug_live ON categories (slug) WHERE deleted_at IS NULL;
CREATE INDEX ix_categories_status ON categories (status);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    status VARCHAR(32) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_tags_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT fk_tags_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_tags_slug_live ON tags (slug) WHERE deleted_at IS NULL;
CREATE INDEX ix_tags_status ON tags (status);

CREATE TABLE technologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT,
    how_i_use_it TEXT,
    is_core BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_technologies_type CHECK (type IN ('LANGUAGE', 'FRAMEWORK', 'DATABASE', 'DEVOPS', 'CLOUD', 'TESTING', 'TOOL', 'OTHER')),
    CONSTRAINT ck_technologies_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT fk_technologies_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_technologies_slug_live ON technologies (slug) WHERE deleted_at IS NULL;
CREATE INDEX ix_technologies_status ON technologies (status);
CREATE INDEX ix_technologies_core ON technologies (is_core);

CREATE TABLE skill_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_skill_groups_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT fk_skill_groups_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_skill_groups_slug_live ON skill_groups (slug) WHERE deleted_at IS NULL;
CREATE INDEX ix_skill_groups_status ON skill_groups (status);

CREATE TABLE skill_group_technologies (
    skill_group_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (skill_group_id, technology_id),
    CONSTRAINT fk_skill_group_technologies_group FOREIGN KEY (skill_group_id) REFERENCES skill_groups (id),
    CONSTRAINT fk_skill_group_technologies_technology FOREIGN KEY (technology_id) REFERENCES technologies (id)
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    language VARCHAR(8) NOT NULL,
    summary TEXT NOT NULL,
    description TEXT,
    role VARCHAR(160),
    project_type VARCHAR(32) NOT NULL,
    project_status VARCHAR(32) NOT NULL,
    content_status VARCHAR(32) NOT NULL,
    problem TEXT,
    solution TEXT,
    backend_highlights TEXT,
    architecture_notes TEXT,
    database_notes TEXT,
    security_notes TEXT,
    challenges TEXT,
    lessons_learned TEXT,
    seo_title VARCHAR(255),
    seo_description VARCHAR(320),
    published_at TIMESTAMPTZ,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_projects_language CHECK (language IN ('EN', 'VI')),
    CONSTRAINT ck_projects_project_type CHECK (project_type IN ('BACKEND', 'FULL_STACK', 'API', 'INFRASTRUCTURE', 'OTHER')),
    CONSTRAINT ck_projects_project_status CHECK (project_status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'MAINTAINED', 'ARCHIVED')),
    CONSTRAINT ck_projects_content_status CHECK (content_status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_projects_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_projects_slug_language_live ON projects (slug, language) WHERE deleted_at IS NULL;
CREATE INDEX ix_projects_content_status ON projects (content_status);

CREATE TABLE technical_notes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    language VARCHAR(8) NOT NULL,
    excerpt TEXT NOT NULL,
    content TEXT NOT NULL,
    category_id BIGINT,
    status VARCHAR(32) NOT NULL,
    seo_title VARCHAR(255),
    seo_description VARCHAR(320),
    reading_minutes INTEGER NOT NULL DEFAULT 1,
    published_at TIMESTAMPTZ,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_notes_language CHECK (language IN ('EN', 'VI')),
    CONSTRAINT ck_notes_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_notes_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_notes_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX ux_notes_slug_language_live ON technical_notes (slug, language) WHERE deleted_at IS NULL;
CREATE INDEX ix_notes_status ON technical_notes (status);
CREATE INDEX ix_notes_category ON technical_notes (category_id);

CREATE TABLE project_technologies (
    project_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, technology_id),
    CONSTRAINT fk_project_technologies_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_technologies_technology FOREIGN KEY (technology_id) REFERENCES technologies (id)
);

CREATE TABLE project_tags (
    project_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, tag_id),
    CONSTRAINT fk_project_tags_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE TABLE note_technologies (
    note_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    PRIMARY KEY (note_id, technology_id),
    CONSTRAINT fk_note_technologies_note FOREIGN KEY (note_id) REFERENCES technical_notes (id),
    CONSTRAINT fk_note_technologies_technology FOREIGN KEY (technology_id) REFERENCES technologies (id)
);

CREATE TABLE note_tags (
    note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (note_id, tag_id),
    CONSTRAINT fk_note_tags_note FOREIGN KEY (note_id) REFERENCES technical_notes (id),
    CONSTRAINT fk_note_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE TABLE project_notes (
    project_id BIGINT NOT NULL,
    note_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, note_id),
    CONSTRAINT fk_project_notes_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_notes_note FOREIGN KEY (note_id) REFERENCES technical_notes (id)
);
