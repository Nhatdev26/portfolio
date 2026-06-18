# Validation

## Proof Strategy

Prove media behavior at service level, frontend compile level, Docker/API smoke
level, and browser interaction level. Public media access must reject non-public
assets, and delete protection must block used assets.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Upload accepts valid image; bad type rejected; used delete returns conflict; public download requires READY + PUBLIC; usage attach/detach audits. |
| Integration | Flyway V8 applies; admin upload/list/update/content/delete APIs pass through Docker; audit events are searchable. |
| E2E | Admin can open `/admin/media`, upload an asset, see grid/detail metadata, and receive used-delete warning. |
| Platform | Docker Compose build/up and backend/frontend proxy health pass. |
| Performance | Not measured in this story; admin list is bounded by current DB query scope. |
| Logs/Audit | MEDIA_UPLOAD, MEDIA_UPDATE, MEDIA_DELETE, MEDIA_USAGE_ATTACH, MEDIA_USAGE_DETACH do not persist file bytes. |

## Fixtures

- Admin seed user from Docker environment.
- A deterministic tiny PNG generated during API smoke.

## Commands

```text
mvn test
cd frontend; npm run typecheck; npm run build
docker compose build
docker compose up -d
API smoke through http://localhost:8081
Browser smoke at /admin/media
```

## Acceptance Evidence

- Backend `mvn test` from `backend/` passed 38/38.
- Frontend `npm run typecheck` passed.
- Frontend `npm run build` passed.
- Harness `story verify US-009` passed with backend tests, frontend typecheck,
  and frontend build.
- `docker compose build` passed for backend and frontend images.
- `docker compose up -d` started db, backend, and frontend with Flyway V8
  applied.
- API smoke passed: admin login, media upload, admin list, metadata update,
  PRIVATE public download 404, PUBLIC public download 200, usage attach,
  used-asset delete 409, and MEDIA_UPLOAD audit search.
- Browser smoke passed at `/admin/media`: desktop and 375px mobile rendered
  media metrics, upload panel, asset grid, inspector, delete protection
  warning, and no console warn/error logs or horizontal overflow.
