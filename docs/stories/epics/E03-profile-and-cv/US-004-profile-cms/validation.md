# Validation

## Proof Strategy

This story is complete when backend profile unit tests pass, frontend
typecheck/build passes, the Docker stack runs migration v3, the profile APIs
work through the frontend proxy, and the browser can save profile content in
admin and read it from public pages.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Save profile/content/social links; reject duplicate languages; reject invalid social URLs; public reads require ACTIVE profile/content and active social links. |
| Integration | Docker backend starts; Flyway reaches schema version 3; admin profile save and public profile read work through frontend proxy. |
| E2E | Admin login; `/admin/profile` edit/save; public `/` and `/about` display saved ACTIVE content. |
| Platform | `docker compose up -d --build backend frontend` starts backend and frontend containers. |
| Performance | Not applicable. |
| Logs/Audit | Profile audit events are deferred. Validation checks no browser console warn/error logs. |

## Fixtures

- `admin@example.com`
- `change-me-in-real-env`
- ACTIVE EN content.
- DRAFT VI content to prove public language/content filtering.

## Commands

```text
mvn test
npm run typecheck
npm run build
docker compose up -d --build backend frontend
curl.exe http://127.0.0.1:5173/api/health
curl.exe -X POST http://127.0.0.1:5173/auth/login
curl.exe -X PUT http://127.0.0.1:5173/api/admin/profile
curl.exe http://127.0.0.1:5173/public/profile?language=EN
```

## Acceptance Evidence

- `mvn test` passed on 2026-06-17 with 16 tests, 0 failures, 0 errors, and 0 skipped.
- `npm run typecheck` passed.
- `npm run build` passed and produced a production Vite bundle.
- Docker backend was rebuilt with `--no-cache`; frontend was rebuilt with
  `--no-cache`; backend/frontend containers were recreated with
  `docker compose up -d --no-build backend frontend`.
- Docker Compose kept `db`, `backend`, and `frontend` containers up.
- Backend log showed 5 JPA repositories, Flyway validated 3 migrations, applied
  `V3__profile_cms.sql`, and schema reached version v3.
- Admin seed skipped because `admin@example.com` already existed.
- `GET /api/health` through the frontend proxy returned `ok`.
- Admin login through the frontend proxy returned a Bearer session for
  `admin@example.com`.
- `PUT /api/admin/profile` through the frontend proxy saved profile id `1` with
  2 localized content rows and 2 active social links.
- `GET /api/admin/profile` returned the saved admin profile document.
- `GET /public/profile?language=EN` returned headline `Building reliable backend
  systems` and 2 active social links.
- `GET /public/profile?language=VI` returned HTTP 404 while VI content was
  DRAFT.
- Browser smoke: `/admin/login` accepted the seed admin credentials; `/admin/profile`
  loaded saved content and displayed `Profile saved.` after submit; public `/`
  showed the saved headline and GitHub link; public `/about` showed `Nhat
  Nguyen` and the long bio; console had 0 warn/error logs.

Known non-blocking validation note:

- `mvn test` still emits the Mockito/ByteBuddy dynamic-agent warning on this
  JDK. Tests pass; this remains a future build-hardening cleanup.
