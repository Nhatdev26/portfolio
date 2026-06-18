# US-014 Security Review Overview

## Current Behavior

The backend has JWT-based admin authentication, public read APIs, media and CV
upload validation, and audit redaction. Prior stories validated these behaviors
inside feature-specific tests, but the security boundary itself did not have a
focused regression packet.

## Target Behavior

Security-sensitive boundaries are reviewed against the product contract and
covered by focused regression tests where practical:

- Admin APIs require authentication.
- Public APIs remain anonymous read-only surfaces.
- Invalid bearer tokens do not authenticate requests.
- Validation errors do not echo secret request values.
- Configured CORS origins support browser preflight without opening arbitrary
  origins.
- File upload and audit redaction behavior remains covered by existing service
  tests.

## Affected Users

- Admin.
- Public visitor.
- System owner.

## Affected Product Docs

- `SPEC.md`
- `docs/product/roadmap.md`
- `docs/decisions/0009-auth-foundation-boundary.md`
- `docs/decisions/0010-admin-login-jwt-session.md`
- `docs/decisions/0014-audit-log-boundary.md`
- `docs/decisions/0015-media-storage-boundary.md`

## Non-Goals

- Replacing local-storage frontend sessions.
- Changing JWT/refresh-token protocol.
- Adding rate limiting or production WAF behavior.
- Changing deployment secret management beyond review evidence.
