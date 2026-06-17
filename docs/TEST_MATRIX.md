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

## Evidence Rules

- Unit proof covers pure domain and application rules.
- Integration proof covers backend enforcement, data integrity, provider
  behavior, jobs, or service contracts.
- E2E proof covers user-visible browser flows.
- Platform proof covers only shell, deployment, mobile, desktop, or runtime
  behavior that cannot be proven in lower layers.
- A story can be implemented without every proof column if the story packet
  explains why.
