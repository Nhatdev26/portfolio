# Design

## Domain Model

Profile:

- Stores display name, email, location, primary role, career direction, main
  tech focus, and status.
- Status values are `DRAFT`, `ACTIVE`, and `INACTIVE`.
- Soft delete columns are present for future admin delete flows.
- The database allows only one non-deleted ACTIVE profile.

Profile Content:

- Belongs to one profile.
- Language values are `EN` and `VI`.
- Status values are `DRAFT`, `ACTIVE`, and `INACTIVE`.
- The database allows only one ACTIVE content row per profile and language.
- Public reads require ACTIVE profile and ACTIVE content for the requested
  language.

Social Link:

- Belongs to one profile.
- Platform values are `GITHUB`, `LINKEDIN`, `EMAIL`, `PORTFOLIO`, and `OTHER`.
- Status values are `ACTIVE` and `INACTIVE`.
- Public reads return only ACTIVE links ordered by display order.
- URL validation requires `mailto:` for `EMAIL` and `http` or `https` for other
  platforms.

## Application Flow

Admin edit:

1. `/admin/profile` requires an authenticated frontend admin session.
2. The page loads `GET /api/admin/profile`.
3. Missing profile data returns an empty editable draft DTO.
4. Save sends the whole profile document to `PUT /api/admin/profile`.
5. The backend validates content language uniqueness before writing content.
6. Omitted existing social links are soft-deleted.

Public read:

1. Public React routes request `GET /public/profile?language=EN`.
2. Backend requires an ACTIVE profile.
3. Backend requires ACTIVE content for the requested language.
4. Backend returns active social links only.
5. Missing active profile/content returns HTTP 404 and the frontend keeps the
   placeholder copy.

## Interface Contract

- `GET /public/profile?language=EN|VI`
  - Public.
  - Response: `{ displayName, email, location, primaryRole, careerDirection,
    mainTechFocus, language, headline, subheadline, shortBio, longBio,
    socialLinks }`.
  - Missing ACTIVE profile/content returns HTTP 404.
- `GET /api/admin/profile`
  - Requires `Authorization: Bearer <accessToken>`.
  - Response: admin profile document with draft/inactive content and links.
- `PUT /api/admin/profile`
  - Requires `Authorization: Bearer <accessToken>`.
  - Request/response: admin profile document.
  - Invalid duplicate content language or social URL returns HTTP 400.

## Data Model

Migration `V3__profile_cms.sql` creates:

- `profiles`
- `profile_contents`
- `social_links`

The migration adds check constraints for enum fields and partial unique indexes
for the active-profile and active-localized-content rules.

## UI / Platform Impact

- Vite and Nginx proxy `/public/*` and `/api/*` to the backend.
- Frontend `/admin/*` remains SPA-owned.
- Backend admin APIs use `/api/admin/**` to avoid conflict with SPA admin
  routes.

## Observability

- Backend validation failures use the shared JSON API error format.
- Browser smoke checks console warn/error logs.
- Audit events are deferred to a later story.

## Alternatives Considered

1. Implement CV upload in the same branch. Deferred to keep the profile slice
   reviewable and testable.
2. Use backend `/admin/profile`. Rejected because Nginx serves frontend
   `/admin/*` SPA routes.
3. Allow multiple active localized content rows and resolve by latest id.
   Rejected because public content selection should be deterministic.
