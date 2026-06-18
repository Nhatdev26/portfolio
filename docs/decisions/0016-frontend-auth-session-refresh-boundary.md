# 0016 Frontend Auth Session Refresh Boundary

Date: 2026-06-18

## Status

Accepted

## Context

US-020 browser QA found that an old admin browser session could keep the admin
shell visible after the access token expired or after the Docker stack was
rebuilt. The backend correctly returned `401` for protected admin APIs, but the
frontend still rendered the stored user and individual CMS pages showed load
errors.

## Decision

The frontend owns stored-session validation and refresh behavior:

- Stored sessions include an `expiresAt` timestamp.
- The auth provider validates or refreshes a stored session during app startup.
- Admin API requests that receive `401` retry once after refreshing the stored
  refresh token.
- A successful refresh updates local storage and dispatches an in-window event
  so the auth provider syncs its React state.
- A failed refresh clears the stored session and sends admin routes back to
  `/admin/login`.

The backend remains the source of truth for token validity, refresh token
rotation, and protected API authorization.

## Alternatives Considered

1. Only validate the stored session on app startup.
2. Require every admin page to handle `401` and refresh manually.
3. Leave stale-session errors visible and ask users to sign out manually.

## Consequences

Positive:

- Admin users do not stay in a misleading signed-in shell after token expiry.
- Existing admin service calls can recover from one stale access token without
  each CMS page duplicating refresh logic.
- Failed refresh tokens cleanly return the user to login.

Tradeoffs:

- The API client now knows the auth storage key and refresh endpoint.
- A later multi-role or multi-account UI should revisit this boundary and
  likely move session handling into a dedicated auth client module.

## Follow-Up

- Add dedicated frontend unit tests if a browser test runner is introduced.
