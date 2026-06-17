# Validation

## Proof Strategy

This story is complete when the auth foundation compiles, tests pass, Docker
Compose starts the system, Flyway applies migration version 2, and existing
public smoke endpoints still work.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Password encoder hashes seed password; seeder creates an ACTIVE ADMIN when enabled and missing; seeder skips existing users. |
| Integration | Flyway applies `V2__auth_foundation.sql`; PostgreSQL has `users`, `refresh_tokens`, and migration history version 2. |
| E2E | Existing public home and admin login shell still render. |
| Platform | `docker compose build` and `docker compose up -d` pass. |
| Performance | Not applicable. |
| Logs/Audit | Seeder logs no secrets; audit table is not part of this story. |

## Fixtures

- `ADMIN_SEED_EMAIL=admin@example.com`
- `ADMIN_SEED_PASSWORD=change-me-in-real-env`

## Commands

```text
mvn test
npm run typecheck
npm run build
docker compose build
docker compose up -d
docker compose exec -T db psql -U portfolio -d portfolio_cms -c "select version, description, success from flyway_schema_history order by installed_rank;"
```

## Acceptance Evidence

- `mvn test` passed on 2026-06-17 with 4 tests, 0 failures, 0 errors, and 0 skipped.
- `npm run typecheck` passed for the frontend after API client/proxy updates.
- `npm run build` passed for the frontend; Vite produced the production bundle.
- `docker compose build` passed for the full stack; `docker compose up -d --build backend` and `docker compose up -d --build frontend` recreated healthy containers.
- `curl.exe -i http://127.0.0.1:8080/api/health` returned HTTP 200 with `{"status":"ok","service":"portfolio-cms-backend"}`.
- `curl.exe -i http://127.0.0.1:5173/api/health` returned HTTP 200 through the Nginx frontend proxy.
- `select version, description, success from flyway_schema_history order by installed_rank;` returned version `1 foundation baseline` and version `2 auth foundation`, both successful.
- PostgreSQL contains `users` and `refresh_tokens`; expected indexes include `ux_users_email_active` and `ux_refresh_tokens_token_hash`.
- Browser smoke at `http://127.0.0.1:5173` rendered `Portfolio CMS`, displayed backend status `OK`, displayed service `portfolio-cms-backend`, and had no console warn/error logs.
- Backend startup log after the security config fix no longer emits Spring Boot's generated default security password; it reports `userDetailsService` and `Admin seed is disabled.`

Known non-blocking validation note:

- `mvn test` emits the standard Mockito/ByteBuddy dynamic-agent warning on the current JDK. Tests still pass; this is a future build-hardening cleanup, not a product failure.
