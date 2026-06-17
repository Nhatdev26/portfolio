# Validation

## Proof Strategy

The story is complete only when backend audit behavior is unit-tested, the
frontend compiles, Docker applies the migration, admin API filters work, and
the browser renders the Audit page without console warning or error logs.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Audit redaction, auth success/failure/logout logging, service query filters |
| Integration | Docker migration and admin API filter smoke |
| E2E | Browser login and `/admin/audit-logs` render/filter/detail |
| Platform | Docker Compose build/up and health |
| Performance | Bounded default page size for audit query |
| Logs/Audit | Passwords/tokens are not persisted in old/new values |

## Fixtures

- Seed admin: `admin@example.com` / `change-me-in-real-env`.
- Existing CMS records created by previous smoke tests.

## Commands

```text
mvn test
npm run typecheck
npm run build
docker compose build backend frontend && docker compose up -d
```

## Acceptance Evidence

- `mvn test` passed with 31 tests.
- `npm run typecheck` passed.
- `npm run build` passed.
- `docker compose build backend frontend && docker compose up -d` passed.
- Docker health through frontend proxy returned `{"status":"ok","service":"portfolio-cms-backend"}`.
- Flyway applied V7 audit logs migration on PostgreSQL.
- API smoke verified login success and login failure audit records, action filter,
  and no raw wrong password or access token in audit JSON.
- Browser smoke verified `/admin/audit-logs` metrics, filters, table, selected
  detail panel, safe JSON blocks, no form errors, and no console warnings/errors.
- Mobile viewport smoke at 375px verified no horizontal page overflow and
  one-column audit layout.
