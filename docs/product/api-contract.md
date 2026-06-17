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
- `GET /public/projects/{slug}?language=EN|VI`
- `GET /public/notes`
- `GET /public/notes/{slug}?language=EN|VI`
- `GET /public/categories`
- `GET /public/tags`
- `GET /public/technologies`
- `GET /public/technologies/{slug}`
- `GET /public/skill-groups`
- `GET /public/categories/{slug}/notes`
- `GET /public/tags/{slug}/notes`
- `GET /public/cv/download?language=EN|VI&targetRole=backend-developer`

## Admin Backend APIs

Current path rule:

- Backend admin CMS APIs use `/api/admin/**`.
- Frontend admin UI routes keep `/admin/*`.

Profile:

- `GET /api/admin/profile`
- `PUT /api/admin/profile`

CV files:

- `GET /api/admin/cv-files`
- `POST /api/admin/cv-files`
- `PATCH /api/admin/cv-files/{id}/activate`
- `PATCH /api/admin/cv-files/{id}/archive`
- `DELETE /api/admin/cv-files/{id}`

Projects:

- `POST /api/admin/projects`
- `GET /api/admin/projects`
- `GET /api/admin/projects/{id}`
- `PUT /api/admin/projects/{id}`
- `PATCH /api/admin/projects/{id}/archive`
- `DELETE /api/admin/projects/{id}`

Technical notes:

- `POST /api/admin/notes`
- `GET /api/admin/notes`
- `GET /api/admin/notes/{id}`
- `PUT /api/admin/notes/{id}`
- `PATCH /api/admin/notes/{id}/archive`
- `DELETE /api/admin/notes/{id}`

Taxonomy:

- `GET /api/admin/taxonomy`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `PATCH /api/admin/categories/{id}/archive`
- `DELETE /api/admin/categories/{id}`
- `POST /api/admin/tags`
- `PUT /api/admin/tags/{id}`
- `PATCH /api/admin/tags/{id}/archive`
- `DELETE /api/admin/tags/{id}`
- `POST /api/admin/technologies`
- `PUT /api/admin/technologies/{id}`
- `PATCH /api/admin/technologies/{id}/archive`
- `DELETE /api/admin/technologies/{id}`
- `POST /api/admin/skill-groups`
- `PUT /api/admin/skill-groups/{id}`
- `PATCH /api/admin/skill-groups/{id}/archive`
- `DELETE /api/admin/skill-groups/{id}`

Media and audit:

- `POST /api/admin/media-assets`
- `GET /api/admin/media-assets`
- `PUT /api/admin/media-assets/{id}`
- `DELETE /api/admin/media-assets/{id}`
- `GET /api/admin/audit-logs`

Audit log filters:

- `GET /api/admin/audit-logs?action=LOGIN_SUCCESS`
- `GET /api/admin/audit-logs?entityType=PROJECT`
- `GET /api/admin/audit-logs?actor=admin@example.com`
- `GET /api/admin/audit-logs?from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z`

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

## Current Phase 3 CV Files CMS

The CV files story exposes admin upload and activation APIs under
`/api/admin/cv-files` plus public download through
`/public/cv/download?language=EN|VI&targetRole=backend-developer`.

Admin CV upload accepts only PDF files up to 5 MB. Upload creates a DRAFT CV.
Activating a CV archives any other ACTIVE CV for the same language and target
role. Public CV download returns only ACTIVE, non-deleted CVs.

## Current Phase 4 Taxonomy CMS

The taxonomy story exposes admin save/archive/delete APIs under
`/api/admin/**` and active-only public APIs under `/public/**`.

Slug uniqueness is enforced among non-deleted records. Skill groups can attach
only ACTIVE technologies. Public taxonomy APIs return only ACTIVE records.

## Current Phase 5 Projects And Notes CMS

The content CMS story exposes project and technical-note admin APIs under
`/api/admin/**` and published-only public APIs under `/public/**`.

Project slugs and note slugs are unique per language among non-deleted records.
Publishing a project requires SEO fields and at least one ACTIVE technology.
Publishing a technical note requires SEO fields and an ACTIVE category.

## Current Phase 6 Audit Logs

The audit story exposes read-only admin audit history through
`GET /api/admin/audit-logs`. The endpoint requires admin authentication and
supports filters by action, entity type, actor email, and date range.

Audit logs are not exposed through public APIs. Audit payloads store safe JSONB
summaries only; passwords, tokens, secrets, and authorization values are
redacted before persistence.
