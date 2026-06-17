# 0013 CV File Storage Boundary

## Status

Accepted

## Context

CV upload is part of the Profile and CV epic, but the media/storage provider
slice is not implemented yet. The system still needs a working admin upload and
public active CV download path.

## Decision

- Store CV PDFs directly in PostgreSQL `bytea` for this slice.
- Accept only `application/pdf` uploads with a service-level 5 MB limit.
- Upload creates DRAFT CV records; activation is an explicit admin action.
- Activation deactivates any other ACTIVE CV for the same language and target
  role before marking the selected file ACTIVE.
- Public download returns only ACTIVE, non-deleted CV files.
- Audit writes remain deferred to E06 because the audit service does not exist.

## Consequences

- The CV workflow is usable and self-contained without media infrastructure.
- Future media work can migrate file bytes to object storage behind the same
  API contract.
- The audit acceptance criterion is documented as a known deferred dependency.
