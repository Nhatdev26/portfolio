# US-021 Real Portfolio Seed Content

## Status

Implemented.

## Lane

Normal.

## Summary

Seed real portfolio content so the public website displays meaningful About,
Projects, Skills, and Blog content from the CMS data model instead of empty
states.

## Acceptance Criteria

- Public profile has active English content for the home/about pages.
- Public technologies include the core backend/full-stack stack used by the
  project.
- Public skills page has grouped active technologies.
- Public project list/detail pages show at least three published projects.
- Public blog list/detail pages show at least three published technical notes.
- Seeded content uses existing public visibility rules: ACTIVE profile/content,
  ACTIVE taxonomy, and PUBLISHED projects/notes.
- Fresh Docker/Flyway environments can reproduce the content without manual
  admin entry.

## Design Notes

- Content is seeded through Flyway migration
  `V9__real_portfolio_seed_content.sql`.
- Seed content is product/demo content, not auth seed data. It does not create
  users, passwords, refresh tokens, or secrets.
- Upserts target existing live slugs/status where supported so local
  environments with previous placeholder content can be refreshed.
- Public contact data uses a portfolio contact email and the existing GitHub
  account URL. Secrets and private identifiers are not stored.

## Validation

Planned proof:

| Check | Expected Proof |
| --- | --- |
| Backend tests | `mvn -f backend/pom.xml test` passes. |
| Frontend build | `npm --prefix frontend run typecheck` and `npm --prefix frontend run build` pass. |
| Docker/Flyway smoke | `scripts/smoke/docker-compose-smoke.ps1` applies V9 and serves frontend/backend. |
| Public API smoke | Public profile, projects, notes, and technologies return seeded content. |
| Browser smoke | `/`, `/about`, `/projects`, `/skills`, `/notes`, and seeded detail routes render without console warn/error logs or horizontal overflow. |

## Evidence

- Backend tests passed: `mvn -f backend/pom.xml test` ran 48 tests with 0
  failures and applied Flyway V9 in Testcontainers.
- Frontend checks passed: `npm --prefix frontend run typecheck` and
  `npm --prefix frontend run build`.
- Docker proof passed from a clean database: `docker compose down -v`, then
  `powershell -ExecutionPolicy Bypass -File scripts/smoke/docker-compose-smoke.ps1`.
- Public API smoke passed through the frontend proxy:
  - `/public/profile?language=EN` returned headline
    `Backend developer building production-minded portfolio systems`.
  - `/public/projects` returned 3 projects:
    `portfolio-cms-system`, `repository-harness-workflow`,
    `secure-admin-content-workflow`.
  - `/public/notes` returned 3 notes:
    `public-admin-api-separation`, `designing-project-publish-workflow`,
    `portfolio-cms-validation-checklist`.
  - `/public/technologies` returned 10 technologies, with 8 core technologies.
  - Detail endpoints for `portfolio-cms-system` and
    `public-admin-api-separation` returned seeded detail content.
- Browser smoke passed on desktop for `/`, `/about`, `/projects`,
  `/projects/portfolio-cms-system`, `/skills`, `/technologies/spring-boot`,
  `/notes`, and `/notes/public-admin-api-separation`: seeded content visible,
  no `.form-error`, no horizontal overflow, and 0 console warn/error logs.
- Browser smoke passed at 375px mobile for `/`, `/about`, `/projects`,
  `/projects/portfolio-cms-system`, `/skills`, `/notes`, and
  `/notes/public-admin-api-separation`: no `.form-error`, no horizontal
  overflow, and 0 console warn/error logs.
