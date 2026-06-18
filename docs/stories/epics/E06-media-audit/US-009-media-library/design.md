# Design

## Domain Model

Media Asset stores uploaded file bytes, file metadata, editorial metadata, a
status, visibility, upload time, and soft-delete timestamp. READY means the
asset can be used; DELETED means it is hidden from admin lists and public
content.

Media Usage stores where a media asset is attached. Entity types are PROJECT,
TECHNICAL_NOTE, TECHNOLOGY, and PROFILE. Usage types are COVER_IMAGE,
THUMBNAIL, SCREENSHOT, DIAGRAM, CONTENT_IMAGE, OG_IMAGE, ICON, and AVATAR.

## Application Flow

Upload validates file presence, maximum size, content type, and basic file
signature where practical. A successful upload creates a READY media asset,
persists bytes in PostgreSQL, and records an audit event. Failed uploads do not
create READY assets.

Metadata update changes only title, alt text, caption, and visibility. Delete
checks `media_usages`; used assets return HTTP 409 with usage context and are
not deleted. Unused assets are soft deleted.

## Interface Contract

Admin API:

- `GET /api/admin/media-assets`
- `POST /api/admin/media-assets` multipart `file`, optional `title`,
  `altText`, `caption`, `visibility`
- `PUT /api/admin/media-assets/{id}`
- `DELETE /api/admin/media-assets/{id}`
- `GET /api/admin/media-assets/{id}/content`
- `POST /api/admin/media-assets/{id}/usages`
- `DELETE /api/admin/media-assets/{id}/usages/{usageId}`

Public API:

- `GET /public/media-assets/{id}/content`

## Data Model

`media_assets` uses BIGSERIAL ids, stores `file_data` as `bytea`, and indexes
status, visibility, and upload time. `media_usages` references media assets and
has a uniqueness constraint across media asset, entity type, entity id, and
usage type.

## UI / Platform Impact

`/admin/media` becomes a polished media library with upload workflow, preview
grid, filters, metadata editor, usage warning, and delete protection feedback.
The page must work at mobile and desktop widths without horizontal layout
overflow.

## Observability

Audit events are recorded for MEDIA_UPLOAD, MEDIA_UPDATE, MEDIA_DELETE,
MEDIA_USAGE_ATTACH, and MEDIA_USAGE_DETACH. Audit payloads must not include file
bytes.

## Alternatives Considered

1. External object storage. Deferred because no provider is configured and the
   current stack already stores CV bytes in PostgreSQL.
2. Hard delete for unused media. Deferred in favor of soft delete to preserve
   auditability and avoid accidental loss.
