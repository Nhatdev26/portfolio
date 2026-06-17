# Design

## Domain Model

User:

- Must be ACTIVE and not soft-deleted to authenticate.
- Password verification uses BCrypt.

Access Token:

- Signed JWT using HS256.
- Contains subject user id and claims for email, role, and status.
- TTL is controlled by `AUTH_ACCESS_TOKEN_MINUTES`.

Refresh Token:

- Raw token is returned only to the client.
- SHA-256 hash is persisted in `refresh_tokens`.
- Refresh rotates by revoking the previous token and creating a new token hash.

## Application Flow

Login:

1. Validate email and password input.
2. Look up a non-deleted user by email.
3. Require ACTIVE status and BCrypt password match.
4. Issue access token and refresh token.
5. Store only refresh token hash with expiry and client IP.

Refresh:

1. Hash the presented refresh token.
2. Find a matching unrevoked, unexpired token.
3. Require the token user to still be ACTIVE and not soft-deleted.
4. Revoke the old token and issue a new session.

Logout:

1. Hash the presented refresh token.
2. If found and not revoked, set `revoked_at` and `revoked_by_ip`.

Frontend:

1. Anonymous `/admin` route redirects to `/admin/login`.
2. Login form calls `/auth/login`.
3. Successful login stores the session in local storage and navigates to
   `/admin`.
4. Sign out calls `/auth/logout`, clears local storage, and returns to login.

## Interface Contract

- `POST /auth/login`
  - Request: `{ "email": "admin@example.com", "password": "..." }`
  - Response: `{ accessToken, refreshToken, tokenType, expiresIn, user }`
  - Invalid credentials return HTTP 401.
- `POST /auth/refresh`
  - Request: `{ "refreshToken": "..." }`
  - Response: same shape as login.
  - Invalid refresh token returns HTTP 401.
- `POST /auth/logout`
  - Request: `{ "refreshToken": "..." }`
  - Response: `{ "revoked": true|false }`
- `GET /auth/me`
  - Requires `Authorization: Bearer <accessToken>`.
  - Response: `{ id, email, role, status }`.

## Data Model

No new migration. This story uses `users` and `refresh_tokens` from
`V2__auth_foundation.sql`.

## UI / Platform Impact

- Vite and Nginx proxy `/auth/*` to the backend.
- The frontend keeps `/admin/*` as SPA routes.
- The backend protects `/admin/**` for future backend admin APIs.

## Observability

- Auth validation uses HTTP status and JSON `ApiError` responses.
- Seeder logs remain secret-free.
- Audit events are deferred.

## Alternatives Considered

1. HTTP-only cookie sessions. Deferred to production hardening.
2. Full refresh retry behavior in the API client. Deferred until admin APIs
   exist.
3. Backend admin API proxy at `/admin/**` through Nginx. Rejected for now
   because it conflicts with frontend SPA `/admin/*` routes.
