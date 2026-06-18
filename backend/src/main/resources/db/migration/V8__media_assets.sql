CREATE TABLE media_assets (
    id BIGSERIAL PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    file_data BYTEA NOT NULL,
    title VARCHAR(255) NOT NULL,
    alt_text VARCHAR(500),
    caption TEXT,
    status VARCHAR(30) NOT NULL CHECK (status IN ('READY', 'DELETED', 'FAILED')),
    visibility VARCHAR(30) NOT NULL CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    uploaded_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_media_assets_status ON media_assets(status);
CREATE INDEX idx_media_assets_visibility ON media_assets(visibility);
CREATE INDEX idx_media_assets_uploaded_at ON media_assets(uploaded_at DESC, id DESC);

CREATE TABLE media_usages (
    id BIGSERIAL PRIMARY KEY,
    media_asset_id BIGINT NOT NULL REFERENCES media_assets(id),
    entity_type VARCHAR(40) NOT NULL CHECK (entity_type IN ('PROJECT', 'TECHNICAL_NOTE', 'TECHNOLOGY', 'PROFILE')),
    entity_id BIGINT NOT NULL,
    usage_type VARCHAR(40) NOT NULL CHECK (usage_type IN ('COVER_IMAGE', 'THUMBNAIL', 'SCREENSHOT', 'DIAGRAM', 'CONTENT_IMAGE', 'OG_IMAGE', 'ICON', 'AVATAR')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (media_asset_id, entity_type, entity_id, usage_type)
);

CREATE INDEX idx_media_usages_asset ON media_usages(media_asset_id);
CREATE INDEX idx_media_usages_entity ON media_usages(entity_type, entity_id);
