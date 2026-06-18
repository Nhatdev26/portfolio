# Test Matrix

This file maps product behavior to proof.

Product behavior is tracked here for human readers and in the durable Harness
matrix for agent workflows. Do not mark a row implemented until tests or
validation evidence exist.

## Status Values

| Status | Meaning |
| --- | --- |
| planned | Accepted as intended behavior, not implemented |
| in_progress | Actively being built |
| implemented | Implemented and proof exists |
| changed | Contract changed after earlier implementation |
| retired | No longer part of the product contract |

## Matrix

| Story | Contract | Unit | Integration | E2E | Platform | Status | Evidence |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US-001 | Full-stack project foundation scaffold | yes | yes | yes | yes | implemented | Frontend install/typecheck/build passed; backend `mvn test` passed; Docker Compose build/up passed; backend health, frontend HTTP, browser smoke, and Flyway baseline migration passed. |
| US-002 | Auth foundation for CMS admin accounts | yes | yes | yes | yes | implemented | Backend `mvn test` passed with admin seeder coverage; Flyway v2 created `users` and `refresh_tokens`; Docker Compose build/up passed; backend health and frontend Nginx `/api/health` proxy returned 200; browser smoke displayed backend `OK` with no console warn/error logs. |
| US-003 | Admin login and JWT session | yes | yes | yes | yes | implemented | Backend `mvn test` passed with JWT/auth service coverage; frontend typecheck/build passed; Docker Compose build/up passed with admin seed; `/auth/login`, `/auth/me`, `/auth/refresh`, and `/auth/logout` passed through the frontend proxy; browser smoke completed login and sign out with no console warn/error logs. |
| US-004 | Profile CMS vertical slice | yes | yes | yes | yes | implemented | Backend `mvn test` passed with profile service coverage; frontend typecheck/build passed; Docker Compose applied Flyway v3; `/api/admin/profile` save/read and `/public/profile` EN/VI visibility checks passed through frontend proxy; browser smoke saved profile and rendered public home/about with no console warn/error logs. |
| US-007 | CV files CMS and public download | yes | yes | yes | yes | implemented | Backend `mvn test` passed with CV service coverage; frontend typecheck/build passed; Docker Compose applied Flyway v6; API smoke uploaded and activated a PDF CV, verified public download, and rejected `.txt`; browser smoke rendered public CV CTA and admin ACTIVE row with no console warn/error logs. |
| US-008 | Audit logs service and UI | yes | yes | yes | yes | implemented | Backend `mvn test` passed with audit redaction and service coverage; frontend typecheck/build passed; Docker Compose applied Flyway v7; API smoke verified login success/failure audit records and no password/token leakage; browser smoke rendered audit metrics, filters, table, detail panel, and mobile layout with no console warn/error logs. |
| US-009 | Media library upload and delete protection | yes | yes | yes | yes | implemented | Backend `mvn test` passed 38/38 with media service coverage; frontend typecheck/build passed; Docker Compose build/up passed with Flyway V8; API smoke verified upload/list/update/public visibility/usage/delete protection/audit; browser smoke rendered desktop and 375px mobile media library with no console warn/error logs or horizontal overflow. |
| US-005 | Taxonomy technology and skill groups | yes | yes | yes | yes | implemented | Backend `mvn test` passed with taxonomy service coverage; frontend typecheck/build passed; Docker Compose applied Flyway v4/v5; API smoke created category/tag/technology/skill group and public technology detail; browser smoke rendered admin taxonomy pages with no console warn/error logs. |
| US-006 | Projects and technical notes CMS | yes | yes | yes | yes | implemented | Backend `mvn test` passed with content service coverage; frontend typecheck/build passed; Docker Compose build/up passed; API smoke published project and note and verified public list/detail endpoints; browser smoke rendered admin/public pages with no console warn/error logs. |

## Evidence Rules

- Unit proof covers pure domain and application rules.
- Integration proof covers backend enforcement, data integrity, provider
  behavior, jobs, or service contracts.
- E2E proof covers user-visible browser flows.
- Platform proof covers only shell, deployment, mobile, desktop, or runtime
  behavior that cannot be proven in lower layers.
- A story can be implemented without every proof column if the story packet
  explains why.
