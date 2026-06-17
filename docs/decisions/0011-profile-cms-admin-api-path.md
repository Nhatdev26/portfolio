# 0011 Profile CMS Admin API Path

Date: 2026-06-17

## Status

Accepted

## Context

The frontend owns `/admin/*` as browser routes for the CMS shell. US-003 noted
that backend admin CRUD APIs need a non-conflicting path before CRUD work lands.
US-004 is the first admin CRUD-style vertical slice, so it must settle the
route boundary.

## Decision

Use `/api/admin/**` for backend admin CMS APIs. The profile vertical slice
exposes:

- `GET /api/admin/profile`
- `PUT /api/admin/profile`

The public API remains:

- `GET /public/profile?language=EN|VI`

Vite and Nginx proxy `/api/*` and `/public/*` to the backend. Frontend
`/admin/*` stays SPA-owned.

## Alternatives Considered

1. Use backend `/admin/**`. Rejected because it conflicts with frontend admin
   routes.
2. Put admin APIs under `/cms/**`. Rejected because `/api/admin/**` is clearer
   and leaves `/admin/*` for the UI.
3. Proxy selected `/admin/api/**` paths. Rejected because it mixes UI and API
   ownership under the same prefix.

## Consequences

Positive:

- Backend APIs and frontend routes no longer compete for `/admin/*`.
- Future CRUD APIs have a stable same-origin path pattern.
- Security config can protect `/api/admin/**` consistently.

Tradeoffs:

- Some earlier product docs and SPEC examples used `/admin/**`; these need to
  be treated as planned intent and mapped to `/api/admin/**` as stories land.

## Follow-Up

- Apply `/api/admin/**` consistently to future project, note, taxonomy, CV,
  media, and audit API stories.
