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

- `GET /public/profile?language=EN|VI`
- `GET /public/projects`
- `GET /public/projects/{slug}`
- `GET /public/notes`
- `GET /public/notes/{slug}`
- `GET /public/technologies/{slug}`
- `GET /public/categories/{slug}/notes`
- `GET /public/tags/{slug}/notes`
- `GET /public/cv/download`

## Admin Backend APIs

Current path rule:

- Backend admin CMS APIs use `/api/admin/**`.
- Frontend admin UI routes keep `/admin/*`.

Profile:

- `GET /api/admin/profile`
- `PUT /api/admin/profile`

Projects:

- `POST /api/admin/projects`
- `GET /api/admin/projects`
- `GET /api/admin/projects/{id}`
- `PUT /api/admin/projects/{id}`
- `PATCH /api/admin/projects/{id}/status`
- `DELETE /api/admin/projects/{id}`

Technical notes:

- `POST /api/admin/notes`
- `GET /api/admin/notes`
- `GET /api/admin/notes/{id}`
- `PUT /api/admin/notes/{id}`
- `PATCH /api/admin/notes/{id}/status`
- `DELETE /api/admin/notes/{id}`

Media and audit:

- `POST /api/admin/media-assets`
- `GET /api/admin/media-assets`
- `PUT /api/admin/media-assets/{id}`
- `DELETE /api/admin/media-assets/{id}`
- `GET /api/admin/audit-logs`

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

## Current Phase 3 Admin Login

The admin login story exposes:

- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

`/auth/me` requires `Authorization: Bearer <accessToken>`.

Backend `/admin/**` routes require authentication for legacy safety. Frontend
`/admin/*` routes remain SPA routes served by the frontend container.

## Current Phase 3 Profile CMS

The profile CMS story exposes:

- `GET /public/profile?language=EN|VI`
- `GET /api/admin/profile`
- `PUT /api/admin/profile`

`/api/admin/profile` requires `Authorization: Bearer <accessToken>`.

The public profile API returns only ACTIVE profile data, ACTIVE localized
content for the requested language, and ACTIVE social links. Missing active
profile/content returns HTTP 404.
