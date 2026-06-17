# 0009 Auth Foundation Boundary

Date: 2026-06-17

## Status

Accepted

## Context

Phase 2 introduces authentication and admin shell behavior. Auth touches
security, authorization, data model, public/admin boundaries, and future audit
coverage. Implementing login, JWT issuance, refresh token rotation, protected
routes, and first-admin persistence in one branch would make review and proof
too broad.

## Decision

Implement auth foundation first:

- Create `users` and `refresh_tokens` tables.
- Store BCrypt password hashes only.
- Store refresh token hashes only.
- Seed the first admin only from environment variables and only when explicitly
  enabled.
- Add Spring Security now, but keep all routes permitted until login/JWT and
  route protection are implemented in later stories.

## Alternatives Considered

1. Implement full login/JWT/protection immediately. Rejected because it mixes
   multiple high-risk behaviors and makes proof harder to isolate.
2. Use a hard-coded development admin. Rejected because secrets must not be
   committed.
3. Delay Spring Security until login APIs. Rejected because password hashing and
   security-chain behavior are foundational enough to validate now.

## Consequences

Positive:

- Auth data model can be reviewed before token behavior exists.
- Future login work can build on tested persistence and hashing.
- Existing Phase 1 health and frontend smoke checks remain stable.

Tradeoffs:

- Admin routes are still not protected in this story.
- The temporary permit-all security chain must be tightened in the login/JWT
  story.

## Follow-Up

- Add login, refresh, logout, and `/auth/me` APIs.
- Tighten Spring Security to require authentication for `/admin/**`.
- Add audit events for login success, login failure, refresh, and logout.

