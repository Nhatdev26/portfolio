# US-016 Full Docker Compose Smoke

## Status

implemented

## Lane

normal

## Product Contract

The full local Docker Compose stack can run PostgreSQL, backend, and frontend
together, and a repeatable smoke command proves that the frontend can reach the
backend through its Nginx proxy.

## Relevant Product Docs

- `SPEC.md`
- `docs/product/roadmap.md`

## Acceptance Criteria

- `docker-compose.yml` includes PostgreSQL.
- `docker-compose.yml` includes backend.
- `docker-compose.yml` includes frontend.
- Backend can reach database.
- Frontend can reach backend.
- System can run from a clean environment.

## Design Notes

- `scripts/smoke/docker-compose-smoke.ps1` starts the Compose stack with
  `docker compose up -d --build` by default.
- The smoke checks backend health at `http://localhost:8080/api/health`.
- The smoke checks the frontend shell at `http://localhost:5173/`.
- The smoke checks frontend-to-backend proxying through
  `http://localhost:5173/api/health`.
- The smoke verifies `db`, `backend`, and `frontend` are running.
- The `-SkipBuild` flag reuses already-built images for faster repeated
  verification.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Not required for smoke script. |
| Integration | Harness story verification using the smoke command. |
| E2E | Frontend proxy health check through the running container. |
| Platform | Docker Compose smoke on the local machine. |
| Release | Not required. |

## Harness Delta

Adds a reusable smoke command for future deployment checks.

## Evidence

- `powershell -ExecutionPolicy Bypass -File scripts/smoke/docker-compose-smoke.ps1`
  passed with build enabled.
- `.\scripts\bin\harness-cli.exe story verify US-016` passed using
  `-SkipBuild`.
- Backend health returned the portfolio backend service through
  `http://localhost:8080/api/health`.
- Frontend HTML shell returned HTTP 200 through `http://localhost:5173/`.
- Frontend Nginx proxy returned backend health through
  `http://localhost:5173/api/health`.
- Compose services `db`, `backend`, and `frontend` were running.
- `.\scripts\bin\harness-cli.exe query tools --capability deploy-verification
  --status present` returned no present providers, so the local smoke script is
  the platform proof for this story.
