# 0012 E04/E05 Content CMS Scope

## Status

Accepted

## Context

E04 and E05 were implemented together because projects and technical notes need
the taxonomy entities before publish validation can be meaningful.

## Decision

- Keep backend admin APIs under `/api/admin/**`; frontend admin pages remain
  under `/admin/*`.
- Implement taxonomy, project, and technical-note soft delete/status rules now.
- Defer audit-log writes to E06 because the audit service and durable audit
  model are not implemented yet.
- Use lightweight markdown display for technical notes until a dedicated
  markdown renderer is selected.
- Add Flyway V5 to align project columns after V4 was applied in local Docker
  during validation.

## Consequences

- E04/E05 can be validated end-to-end now.
- Audit behavior remains a documented future slice rather than a partial stub.
- Public note rendering is safe and dependency-free, but not full markdown.
