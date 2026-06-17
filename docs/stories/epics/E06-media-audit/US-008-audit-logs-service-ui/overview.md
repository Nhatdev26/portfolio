# US-008 Audit Logs Service And UI

## Current Behavior

Important admin actions are not persisted as product audit records. The admin
sidebar has an Audit route, but it still renders a placeholder page.

## Target Behavior

Admin actions create read-only audit log records with actor, action, entity,
result, timestamp, and safe old/new JSON values. Admin users can filter and
inspect audit logs from `/admin/audit-logs`. Public APIs do not expose audit
logs.

## Affected Users

- CMS admin.
- System owner reviewing CMS activity.

## Affected Product Docs

- `docs/product/api-contract.md`
- `docs/product/content-model.md`

## Non-Goals

- Media asset upload and media usage tracking.
- External log export or retention automation.
- Fine-grained diff visualization beyond readable JSON values.
