# 0010 Admin Login JWT Session

Date: 2026-06-17

## Status

Accepted

## Context

After the auth foundation, the product needs the first working admin session:
login, current-user lookup, refresh token rotation, logout, and protected admin
surface behavior. The backend already has `users` and `refresh_tokens`, and the
frontend has a static login form and admin shell.

## Decision

Implement the first admin session boundary:

- Issue signed HS256 JWT access tokens from `/auth/login`.
- Keep access tokens short-lived through `AUTH_ACCESS_TOKEN_MINUTES`.
- Generate opaque refresh tokens and store only SHA-256 hashes.
- Rotate refresh tokens on `/auth/refresh` by revoking the old token and
  persisting a new hash.
- Revoke refresh tokens on `/auth/logout`.
- Require Bearer authentication for `/auth/me` and backend `/admin/**` routes.
- Store the current browser session in frontend local storage for this local MVP
  phase.

## Alternatives Considered

1. HTTP-only refresh-token cookies. Deferred because this branch needs a small,
   reviewable auth foundation before deployment-domain and CSRF policy are
   finalized.
2. Server-side sessions. Rejected because the product contract calls for JWT
   access tokens and refresh tokens.
3. Leaving admin routes unprotected until CRUD APIs exist. Rejected because the
   login story is the right point to tighten backend admin route protection.

## Consequences

Positive:

- Admin login now works end to end.
- Refresh tokens are never stored raw.
- `/auth/me` gives frontend and future admin APIs a stable current-user
  contract.

Tradeoffs:

- Frontend local-storage session is acceptable for local MVP validation but
  should be revisited before production hardening.
- Backend `/admin/**` is protected, while frontend `/admin/*` remains an SPA
  route served by Nginx. Future same-origin admin APIs need a non-conflicting
  proxy/API path decision.

## Follow-Up

- Add audit events for login success, login failure, refresh, and logout.
- Add token refresh retry behavior in the frontend API client.
- Decide whether production admin API paths use `/api/admin/**` to avoid SPA
  route conflicts.
