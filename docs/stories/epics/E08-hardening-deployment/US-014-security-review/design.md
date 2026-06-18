# US-014 Security Review Design

## Domain Model

No domain model changes are planned. Existing security domain objects remain:

- `User`
- `RefreshToken`
- `AuthenticatedUser`
- `AuditLog`
- `MediaAsset`
- `CvFile`

## Application Flow

The review preserves the existing flow:

1. Public APIs are anonymous reads.
2. Admin APIs under `/api/admin/**` require a valid bearer access token.
3. `JwtAuthenticationFilter` parses the token, then reloads the user and
   requires the user to still be active and not deleted.
4. Auth APIs issue and rotate JWT/refresh-token sessions.
5. Audit writes sanitize old/new payloads before persistence.

## Interface Contract

No API contract change is planned. Regression tests cover:

- Anonymous `GET /api/admin/audit-logs` returns `401`.
- Invalid bearer token on `GET /api/admin/audit-logs` returns `401`.
- Anonymous `GET /auth/me` returns `401`.
- Anonymous `GET /public/projects` remains allowed.
- Allowed local frontend origin can preflight admin APIs.
- Login validation errors do not echo the submitted password value.

## Data Model

No migration is planned.

## UI / Platform Impact

No frontend UI change is planned. CORS proof protects browser access from the
configured local frontend origin.

## Observability

No new audit action is added. Existing audit redaction remains the product
record boundary.

## Alternatives Considered

1. Full end-to-end browser security test. Deferred because the selected risk is
   backend boundary behavior and MockMvc gives faster deterministic proof.
2. Runtime behavior changes during review. Rejected unless a concrete defect is
   found.
