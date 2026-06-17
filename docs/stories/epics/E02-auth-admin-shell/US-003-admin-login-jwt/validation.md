# Validation

## Proof Strategy

This story is complete when backend auth unit tests pass, frontend builds, the
Docker stack can run with admin seed enabled, auth endpoints work through the
frontend proxy, and the browser can complete login and logout.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | JWT create/parse/hash; login success; invalid password; refresh rotation; logout revoke. |
| Integration | Docker backend starts; Flyway remains at v2; admin seed creates ACTIVE ADMIN; `/auth/*` works through frontend proxy. |
| E2E | Anonymous `/admin` redirects to login; valid login reaches dashboard; sign out returns to login. |
| Platform | `docker compose up -d --build backend frontend` starts backend and frontend containers. |
| Performance | Not applicable. |
| Logs/Audit | No generated Spring security password; seed logs no secrets. Audit events deferred. |

## Fixtures

- `ADMIN_SEED_ENABLED=true`
- `ADMIN_SEED_EMAIL=admin@example.com`
- `ADMIN_SEED_PASSWORD=change-me-in-real-env`

## Commands

```text
mvn test
npm run typecheck
npm run build
$env:ADMIN_SEED_ENABLED='true'; docker compose up -d --build backend frontend
curl.exe -i http://127.0.0.1:5173/api/health
curl.exe -X POST http://127.0.0.1:5173/auth/login
curl.exe http://127.0.0.1:5173/auth/me
curl.exe -X POST http://127.0.0.1:5173/auth/refresh
curl.exe -X POST http://127.0.0.1:5173/auth/logout
```

## Acceptance Evidence

- `mvn test` passed on 2026-06-17 with 11 tests, 0 failures, 0 errors, and 0 skipped.
- `npm run typecheck` passed.
- `npm run build` passed and produced a production Vite bundle.
- Docker Compose rebuilt and recreated backend/frontend with `ADMIN_SEED_ENABLED=true`; containers stayed up.
- Backend log showed Flyway schema version 2, Tomcat started, and admin seed created the initial admin account without logging secrets.
- PostgreSQL contained `admin@example.com` with role `ADMIN` and status `ACTIVE`.
- `curl.exe -i http://127.0.0.1:5173/api/health` returned HTTP 200.
- `POST /auth/login` through the frontend proxy returned Bearer token data for `admin@example.com`.
- `GET /auth/me` with the Bearer token returned email `admin@example.com`, role `ADMIN`, and status `ACTIVE`.
- `POST /auth/refresh` returned a different refresh token and revoked the previous token.
- `POST /auth/logout` returned `revoked=true`.
- Invalid password returned HTTP 401.
- Unauthenticated `GET /auth/me` returned HTTP 401.
- Browser smoke: `/admin` redirected to `/admin/login`; login reached `/admin` dashboard with `admin@example.com`; Sign out returned to `/admin/login`; console had no warn/error logs.
- After validation, backend was restarted with default `ADMIN_SEED_ENABLED=false`; login still passed using the seeded DB user.

Known non-blocking validation note:

- `mvn test` still emits the Mockito/ByteBuddy dynamic-agent warning on this
  JDK. Tests pass; this remains a future build-hardening cleanup.
