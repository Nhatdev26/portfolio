# US-020 Full Regression QA Pass

## Status

implemented

## Lane

normal, escalated during implementation because QA found an auth/session defect

## Product Contract

Before production deployment work starts, the MVP has a full regression and QA
pass covering backend tests, frontend typecheck/build, production env validation,
Docker Compose full-build smoke, browser public routes, admin session handling,
admin CMS routes, mobile overflow, and console health.

## Relevant Product Docs

- `SPEC.md`
- `docs/deployment/RELEASE_RUNBOOK.md`
- `.github/workflows/ci.yml`

## Acceptance Criteria

- Backend test suite passes.
- Frontend typecheck passes.
- Frontend production build passes.
- Production env template validation passes.
- Docker Compose smoke passes with a full image build.
- Public routes render on desktop and mobile without page-level horizontal
  overflow.
- Admin login/logout works.
- Admin protected APIs reject anonymous requests.
- Admin CMS routes render without stale-session API errors after login.
- Admin table views do not create page-level horizontal overflow on mobile.
- Browser console has no warn/error logs during the QA pass.

## Findings And Fixes

- Browser QA found stale admin sessions could keep the dashboard visible while
  admin API requests failed after token expiry or container rebuild. The
  frontend now validates stored sessions, refreshes tokens, retries one failed
  admin request after refresh, syncs session state after storage updates, and
  clears invalid sessions.
- Browser QA found `/admin/projects` overflowed the mobile viewport because its
  table missed the existing `.data-region` scroll wrapper. Projects and Notes
  tables now use the same responsive table wrapper as CV, taxonomy, and audit
  tables.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id US-020 --unit 1 --integration 1 --e2e 1 --platform 1`.

| Layer | Expected proof |
| --- | --- |
| Unit | Backend tests and frontend typecheck/build. |
| Integration | Production env check and auth refresh/session retry behavior. |
| E2E | Browser QA for public/admin routes and login/logout. |
| Platform | Docker Compose full-build smoke. |

## Evidence

- `mvn -f backend/pom.xml test` passed: 48 tests, 0 failures.
- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run build` passed.
- `powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template`
  passed.
- `powershell -ExecutionPolicy Bypass -File scripts/smoke/docker-compose-smoke.ps1`
  passed after rebuilding backend and frontend images.
- Browser QA passed on cache-busted production bundle `index-CZVnU5MC.js` and
  later mobile retest bundle `index-DdAS7pc9.js`.
- Desktop public routes `/`, `/about`, `/projects`, `/skills`, and `/notes`
  rendered without form errors, console warn/error logs, or horizontal overflow.
- Desktop admin routes `/admin`, `/admin/profile`, `/admin/projects`,
  `/admin/notes`, `/admin/technologies`, `/admin/categories`, `/admin/tags`,
  `/admin/skill-groups`, `/admin/cv-files`, `/admin/media`, and
  `/admin/audit-logs` rendered without form errors, console warn/error logs, or
  horizontal overflow after auth refresh/login.
- Mobile retest passed for `/admin/projects`, `/admin/notes`, `/admin`,
  `/admin/audit-logs`, `/`, `/projects`, and `/notes` with no page-level
  horizontal overflow and no console warn/error logs.
- Admin API protection passed: unauthenticated `/api/admin/profile` returned
  `401` through both the frontend proxy and backend direct URL.
- Admin UI logout/login smoke passed with `admin@example.com` and the local seed
  password, returning to `/admin` with no form errors or console warn/error logs.
