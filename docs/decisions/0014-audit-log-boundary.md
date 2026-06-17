# 0014 Audit Log Boundary

## Status

Accepted

## Context

E06 introduces product audit records for important CMS actions. These records
must be useful for review while avoiding accidental persistence of passwords,
tokens, or other secrets.

## Decision

- Store audit logs in PostgreSQL `audit_logs` with JSONB old/new value columns.
- Audit records are immutable and exposed only through authenticated admin API.
- Actor data comes from the authenticated principal when available; auth events
  pass the attempted or resolved email explicitly.
- Old/new payloads are safe summaries, not full request bodies.
- A recursive redaction step removes sensitive keys such as password, token,
  refreshToken, accessToken, authorization, and passwordHash before persistence.
- Public APIs never expose audit logs.

## Consequences

- Audit coverage can be added incrementally by command services.
- Admin users get actionable activity history without storing secrets.
- Detailed object diffing can be improved later without changing the public
  audit contract.
