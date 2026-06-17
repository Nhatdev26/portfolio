# Validation

## Proof Strategy

Run frontend static validation and browser smoke. Because this is visual/UI
work, browser checks must include desktop and mobile layout with console logs.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Not applicable; no domain logic changes. |
| Integration | Frontend typecheck and production build. |
| E2E | Browser smoke for `/`, `/about`, `/projects`, `/notes`, `/cv`, and an admin route. |
| Platform | 375px mobile and desktop no horizontal overflow. |
| Performance | Build bundle size reviewed qualitatively; no new runtime dependency. |
| Logs/Audit | Browser console has no warn/error logs. |

## Fixtures

- Existing local seeded/admin data and any published public content already in
  the Docker database.

## Commands

```text
cd frontend; npm run typecheck; npm run build
Browser smoke at desktop and 375px mobile
```

## Acceptance Evidence

- `npm run typecheck` passed from `frontend/`.
- `npm run build` passed from `frontend/`.
- Harness `story verify US-011` passed with frontend typecheck and build.
- `docker compose build frontend` passed and `docker compose up -d frontend`
  served the new bundle on `localhost:5173`.
- Browser smoke passed for `/`, `/about`, `/projects`, `/notes`, `/cv`, and
  `/admin/technologies` on desktop with no local console warn/error logs and no
  horizontal overflow.
- Browser smoke passed at 375px mobile for `/`, `/projects`, and
  `/admin/technologies` with no local console warn/error logs and no horizontal
  overflow.
- Additional responsive checks passed at 768px, 1024px, and 1440px.
