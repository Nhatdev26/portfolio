# US-013 - Backend Integration Tests

## Summary

Add backend integration coverage for critical CMS workflows using the real
Spring web layer, Flyway migrations, JPA, security filters, and a PostgreSQL
database.

## Source

- `SPEC.md` Phase 8, US-8.1 - Backend Integration Tests.

## Acceptance Criteria

- Auth login test exists.
- Refresh token test exists.
- Public visibility test exists.
- Project publish workflow test exists.
- Technical Note publish workflow test exists.
- CV active rule test exists.
- Media delete protection test exists.
- Audit log test exists.
- Integration tests run from the standard backend Maven test command.
- Tests use PostgreSQL-compatible schema behavior rather than an incompatible
  in-memory approximation.

## Implementation Notes

- Add Testcontainers PostgreSQL dependencies for test scope.
- Add `CriticalWorkflowIntegrationTests` using `MockMvc` against the full
  Spring Boot application context.
- Keep test data unique per run to avoid ordering and reuse assumptions.
- Preserve existing unit and service tests.

## Discovered Defect

The integration suite exposed a PostgreSQL partial unique index issue in CV
activation: activating a second CV for the same language and role could violate
`ux_cv_files_active_language_role` before the old active CV archive update was
flushed. The service now flushes archived active CV rows before marking the new
CV active.

## Validation Plan

- `mvn -f backend/pom.xml -Dtest=CriticalWorkflowIntegrationTests test`
- `mvn -f backend/pom.xml test`
- `scripts/bin/harness-cli story verify US-013`
- `docker compose build backend`
- `docker compose up -d backend`
