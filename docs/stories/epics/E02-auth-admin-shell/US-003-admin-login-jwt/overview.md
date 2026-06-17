# Admin Login And JWT

## Current Behavior

The auth foundation has admin users and refresh token persistence, but the app
does not expose login, refresh, logout, or current-user APIs. The frontend admin
login form is static and admin routes are not backed by a real session.

## Target Behavior

- Admin can sign in with email and password.
- Backend issues a short-lived JWT access token and opaque refresh token.
- Refresh tokens are stored only as hashes and rotate on refresh.
- Logout revokes the current refresh token.
- `/auth/me` returns the authenticated admin user.
- Frontend `/admin` redirects anonymous users to `/admin/login`.
- Frontend login stores the session and displays the admin dashboard.

## Affected Users

- Admin.
- Future agent/developer implementing CMS CRUD APIs.

## Affected Product Docs

- `docs/product/api-contract.md`
- `docs/product/architecture.md`
- `docs/product/content-model.md`

## Non-Goals

- CMS CRUD APIs.
- Audit log writes for auth events.
- Password reset.
- Production cookie/CSRF hardening.
