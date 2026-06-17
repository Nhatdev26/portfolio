# Validation

## Automated

- `mvn test` passed with 24 tests.
- `npm run typecheck` passed.
- `npm run build` passed.
- `docker compose build backend frontend` passed.

## Runtime

- Docker Compose applied Flyway V4 and V5.
- API smoke created category, tag, technology, and skill group through
  `/api/admin/**`.
- Public API returned active technology detail.
- Browser smoke rendered admin technologies, categories, and skill groups with
  one row each and no console warn/error logs.
