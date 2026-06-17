# Design

## Domain Model

`AuditLog` stores immutable CMS activity records. Actions are typed strings so
the UI can filter known activity without changing the migration for every new
admin workflow. Results are `SUCCESS` or `FAILURE`.

## Application Flow

Command services call `AuditService` after successful mutations. Auth records
login success, login failure, and logout explicitly because unauthenticated
requests do not have a security context. Authenticated admin actions resolve
actor id and email from `SecurityContext`.

## Interface Contract

- `GET /api/admin/audit-logs`
- Query filters: `action`, `entityType`, `actor`, `from`, `to`
- Response includes id, actor email, action, entity type/id/title, result,
  createdAt, oldValue, and newValue.

## Data Model

`audit_logs` uses BIGINT ids, actor metadata columns, action/entity/result
columns, JSONB `old_value` and `new_value`, and timestamp indexes for admin
review.

## UI / Platform Impact

The Audit page is read-only. It uses a denser dashboard-style layout: summary
metrics, filter controls, accessible status chips, responsive table, and a
detail panel for safe JSON values. Frontend follows `ui-ux-pro-max` guidance:
clear labels, visible focus, responsive layout, no emoji icons, readable
contrast, and no layout-shifting hover states.

## Observability

Audit records are product records, not application logs. Sensitive fields such
as password, token, refreshToken, accessToken, and passwordHash are recursively
redacted before persistence.

## Alternatives Considered

1. Log only auth events first. Rejected because the spec requires major admin
   actions and the previous stories deferred those audit writes to E06.
2. Store old/new values as plain text. Rejected because the product model calls
   for JSONB values.
