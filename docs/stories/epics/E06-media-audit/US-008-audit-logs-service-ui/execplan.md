# Exec Plan

## Goal

Implement the audit log service, admin API, and polished read-only admin UI for
E06 audit behavior.

## Scope

In scope:

- `audit_logs` migration, entity, repository, service, and DTOs.
- Audit writes for auth, profile, taxonomy, content, and CV actions.
- Admin audit API with filters.
- `/admin/audit-logs` UI with filters and detail view.
- Product docs, test matrix, Harness records, and validation evidence.

Out of scope:

- Media asset upload and media usage tracking.
- Audit retention jobs and external export.

## Risk Classification

Risk flags:

- Audit/security.
- Data model.
- Public contracts.
- Existing behavior.
- Multi-domain.

Hard gates:

- Audit/security.
- Migration.

## Work Phases

1. Discovery.
2. Design and decision record.
3. Backend audit persistence and service hooks.
4. Admin API and frontend service/page.
5. Verification: unit, typecheck/build, Docker/API/browser smoke.
6. Harness update and PR.

## Stop Conditions

Pause for human confirmation if:

- Audit data would need to store secrets or raw tokens.
- Validation requirements need to be weakened.
- Media scope must be included in the same PR.
