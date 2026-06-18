# 0015 Media Storage Boundary

## Status

Accepted

## Context

E06 introduces Media Asset and Media Usage behavior. The existing application
already stores CV file bytes in PostgreSQL and has no configured object storage
provider, signing service, image transformer, or CDN.

## Decision

For the current CMS build, media bytes are stored in PostgreSQL `bytea` columns.
Admin media APIs require authentication. Public media bytes are exposed only
through `GET /public/media-assets/{id}/content` when the asset is READY,
PUBLIC, and not soft deleted.

Media deletion is soft delete. Used media is protected by `media_usages` and
returns HTTP 409 instead of deleting.

## Consequences

- The implementation remains deterministic for local Docker, tests, and browser
  smoke.
- File size is capped at 10 MB to keep database storage bounded.
- Future object storage can be introduced behind the media service without
  changing the admin media UI contract.

## Validation

US-009 must prove upload, metadata update, visibility enforcement, usage
protection, audit events, and admin UI behavior.
