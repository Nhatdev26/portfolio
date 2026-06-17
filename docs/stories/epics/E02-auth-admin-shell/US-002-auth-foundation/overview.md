# Auth Foundation

## Current Behavior

The backend has no persistent admin user model. The Admin Dashboard shell exists
only as a frontend route. There is no `users` table, no refresh token table, no
password hashing component, and no deterministic way to seed the first admin.

## Target Behavior

The backend has the minimum authentication foundation needed before login APIs:

- `users` table for CMS/admin accounts only.
- `refresh_tokens` table for future refresh-token flow.
- User role and status enums in code.
- JPA entities and repository for admin users.
- BCrypt password hashing support.
- Environment-driven first-admin seed support.

## Affected Users

- Admin.
- Future agent/developer implementing login and protected routes.

## Affected Product Docs

- `docs/product/architecture.md`
- `docs/product/content-model.md`
- `docs/product/api-contract.md`
- `docs/product/roadmap.md`

## Non-Goals

- No login endpoint in this story.
- No JWT access-token issuance in this story.
- No refresh-token rotation in this story.
- No frontend login form submission in this story.
- No protected admin route enforcement in this story.

