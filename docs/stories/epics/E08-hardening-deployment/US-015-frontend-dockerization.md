# US-015 Frontend Dockerization

## Status

implemented

## Lane

normal

## Product Contract

The React frontend can be built into a deterministic Nginx container and served
with a runtime-configurable API base URL. Local Docker Compose can keep using
same-origin proxy paths, while deployed containers may set
`PORTFOLIO_API_BASE_URL` without rebuilding the image.

## Relevant Product Docs

- `SPEC.md`
- `docs/product/roadmap.md`

## Acceptance Criteria

- Frontend Dockerfile exists.
- Frontend build succeeds.
- Frontend can be served in container.
- API base URL is configurable by environment variable.

## Design Notes

- Docker build uses `npm ci` for reproducible installs.
- `frontend/public/env.js` provides a local-dev default config.
- `frontend/docker-entrypoint.d/40-env-js.sh` writes `/env.js` at Nginx
  startup from `PORTFOLIO_API_BASE_URL`, falling back to `VITE_API_BASE_URL`.
- `apiClient.ts` reads `window.__PORTFOLIO_CONFIG__.API_BASE_URL` first, then
  falls back to Vite build-time configuration.
- Docker Compose passes `PORTFOLIO_API_BASE_URL` to the frontend container.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Frontend typecheck/build. |
| Integration | Harness story verification. |
| E2E | Not required for container packaging. |
| Platform | Docker frontend build/up and HTTP smoke for `/` and `/env.js`. |
| Release | Not required. |

## Harness Delta

No Harness policy change.

## Evidence

- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run build` passed.
- `.\scripts\bin\harness-cli.exe story verify US-015` passed.
- `docker compose build frontend` passed.
- `docker compose up -d frontend` served `/` with HTTP 200.
- `/env.js` returned `API_BASE_URL: "http://backend:8080"` when
  `PORTFOLIO_API_BASE_URL` was set for the smoke test.
- Frontend container was restarted with blank `PORTFOLIO_API_BASE_URL` after
  the smoke test so local browser usage keeps same-origin proxy behavior.
