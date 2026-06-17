# Validation

## Automated

- `mvn test` passed with 29 tests.
- `npm run typecheck` passed.
- `npm run build` passed.
- `docker compose build backend frontend && docker compose up -d` passed.
- Docker health through frontend proxy returned `{"status":"ok","service":"portfolio-cms-backend"}`.
- API smoke passed for admin PDF upload, activation, active CV listing, public PDF download, and invalid `.txt` rejection.
- Browser smoke passed for `/cv`, `/`, and `/admin/cv-files`; CV CTA and admin ACTIVE row rendered with no console warning or error logs.

## Deferred

- Upload/activation audit writes are deferred to E06 because the audit service has not been implemented yet.
