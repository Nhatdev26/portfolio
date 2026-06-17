# Portfolio CMS API And Route Contract

## Public Frontend Routes

- `/`
- `/about`
- `/projects`
- `/projects/:slug`
- `/notes`
- `/notes/:slug`
- `/technologies/:slug`
- `/cv`

## Admin Frontend Routes

- `/admin/login`
- `/admin`
- `/admin/profile`
- `/admin/projects`
- `/admin/projects/new`
- `/admin/projects/:id/edit`
- `/admin/notes`
- `/admin/notes/new`
- `/admin/notes/:id/edit`
- `/admin/technologies`
- `/admin/categories`
- `/admin/tags`
- `/admin/skill-groups`
- `/admin/cv-files`
- `/admin/media`
- `/admin/audit-logs`

## Public Backend APIs

- `GET /public/profile`
- `GET /public/projects`
- `GET /public/projects/{slug}`
- `GET /public/notes`
- `GET /public/notes/{slug}`
- `GET /public/technologies/{slug}`
- `GET /public/categories/{slug}/notes`
- `GET /public/tags/{slug}/notes`
- `GET /public/cv/download`

## Admin Backend APIs

Projects:

- `POST /admin/projects`
- `GET /admin/projects`
- `GET /admin/projects/{id}`
- `PUT /admin/projects/{id}`
- `PATCH /admin/projects/{id}/status`
- `DELETE /admin/projects/{id}`

Technical notes:

- `POST /admin/notes`
- `GET /admin/notes`
- `GET /admin/notes/{id}`
- `PUT /admin/notes/{id}`
- `PATCH /admin/notes/{id}/status`
- `DELETE /admin/notes/{id}`

Media and audit:

- `POST /admin/media-assets`
- `GET /admin/media-assets`
- `PUT /admin/media-assets/{id}`
- `DELETE /admin/media-assets/{id}`
- `GET /admin/audit-logs`

Auth:

- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

## Current Phase 1 Temporary API

The foundation scaffold exposes:

- `GET /api/health`

This is a local smoke endpoint only. It is not a final public product API.

## Current Phase 2 Auth Foundation

The auth foundation adds admin user persistence and refresh-token storage only.
It does not expose login, refresh, logout, or `/auth/me` endpoints yet.

Spring Security is present, but route protection remains a later story so the
Phase 1 smoke contract stays unchanged while the auth data model is introduced.
