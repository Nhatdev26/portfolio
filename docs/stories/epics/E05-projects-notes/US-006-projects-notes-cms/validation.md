# Validation

## Automated

- `mvn test` passed with 24 tests.
- `npm run typecheck` passed.
- `npm run build` passed.
- `docker compose build backend frontend` passed.

## Runtime

- API smoke created and published one project and one technical note through the
  frontend proxy.
- `/public/projects`, `/public/projects/{slug}`, `/public/notes`, and
  `/public/notes/{slug}` returned the published records.
- Browser smoke rendered public projects, notes, and technology detail.
- Browser smoke rendered admin project, note, and taxonomy CMS pages after
  fresh login with no console warn/error logs.
