# US-001 Full-stack Project Foundation

## Status

implemented

## Lane

normal

## Product Contract

Create the first runnable shape of the Portfolio CMS system: a Spring Boot
backend, a React Vite frontend, configurable API client, Flyway migration
folder, and Docker Compose services for PostgreSQL, backend, and frontend.

This story intentionally avoids domain CRUD, auth, authorization, and durable
product schema. Those begin in later stories.

## Relevant Product Docs

- `docs/product/overview.md`
- `docs/product/architecture.md`
- `docs/product/api-contract.md`
- `docs/product/roadmap.md`

## Acceptance Criteria

- Backend project exists under `backend/`.
- Backend has a Spring Boot application entrypoint.
- Backend has a health endpoint for local smoke checks.
- Backend has global exception handling.
- Backend has CORS configured for the local React frontend.
- Backend has PostgreSQL and Flyway configuration through environment
  variables.
- Frontend project exists under `frontend/`.
- Frontend has Vite, React, TypeScript, React Router, and TanStack Query setup.
- Frontend has PublicLayout, AdminLayout, Login, Dashboard, Home, and NotFound
  routes.
- Frontend has a configurable fetch-based API client.
- Docker Compose defines PostgreSQL, backend, and frontend services.
- Validation evidence records current toolchain gaps if runtime tools are not
  available locally.

## Design Notes

- Commands: backend writes are deferred to later service stories.
- Queries: frontend uses TanStack Query for the health smoke query.
- API: `GET /api/health` is a temporary local smoke endpoint only.
- Tables: no domain tables are created in Phase 1.
- Domain rules: public/admin data separation is documented but not enforced
  until auth and content stories exist.
- UI surfaces: public and admin route shells exist so future slices have a
  stable place to land.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | `mvn test` after Java and Maven are installed. |
| Integration | Backend starts against PostgreSQL and Flyway runs. |
| E2E | Browser smoke checks public home and admin login after Node is installed. |
| Platform | `docker compose up --build` after Docker is installed. |
| Release | Not applicable for Phase 1. |

## Harness Delta

- Product docs were derived from `SPEC.md`.
- Durable story row should track Phase 1 proof status.
- Toolchain absence is captured as validation evidence, not ignored.

## Evidence

- Static required-file check passed on 2026-06-17.
- `backend/pom.xml` parsed as XML; artifact id is `portfolio-cms`, Spring Boot
  parent version is `3.5.15`.
- `frontend/package.json` parsed as JSON; build script is `tsc -b && vite build`,
  React dependency is `^19.2.7`.
- Conflict-marker and work-marker scan found no matches.
- `npm install` passed in `frontend/`; 31 packages audited with 0
  vulnerabilities.
- `npm run typecheck` passed in `frontend/`.
- `npm run build` passed in `frontend/`; Vite built `dist/`.
- `mvn test` passed in `backend/`; 1 test run, 0 failures, 0 errors.
- Browser smoke passed for `http://127.0.0.1:5173/`; home route rendered
  `Portfolio CMS` and no console errors were reported.
- Browser smoke passed for `http://127.0.0.1:5173/admin/login`; login route
  rendered sign-in fields and no console errors were reported.
- `docker compose config` passed and rendered all services.
- `docker compose build` passed for backend and frontend images.
- Added backend and frontend `.dockerignore` files, then reran
  `docker compose build` successfully with reduced build contexts.
- `docker compose up -d` started PostgreSQL, backend, and frontend services.
- PostgreSQL container reported healthy.
- Backend container started on port `8080`.
- Frontend container started on port `5173`.
- `GET http://127.0.0.1:8080/api/health` returned
  `{"status":"ok","service":"portfolio-cms-backend"}`.
- `GET http://127.0.0.1:5173` returned HTTP 200.
- Browser smoke passed against the Docker-served frontend at
  `http://127.0.0.1:5173/`; `Portfolio CMS` rendered and no console errors
  were reported.
- Backend logs show Flyway validated and applied migration version
  `1 - foundation baseline`.
- Database query confirmed `flyway_schema_history` has version `1`, description
  `foundation baseline`, and `success = true`.
