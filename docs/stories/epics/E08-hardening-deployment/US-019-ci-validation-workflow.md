# US-019 CI Validation Workflow

## Status

implemented

## Lane

normal

## Product Contract

Pull requests and pushes to `main` run the release validation path in GitHub
Actions: backend tests, frontend typecheck/build, production env template
validation, and Docker Compose smoke.

## Relevant Product Docs

- `SPEC.md`
- `docs/deployment/RELEASE_RUNBOOK.md`
- `.github/workflows/ci.yml`

## Acceptance Criteria

- GitHub Actions workflow runs on pull requests.
- GitHub Actions workflow runs on pushes to `main`.
- Backend Maven tests run in CI.
- Frontend dependencies install with `npm ci`.
- Frontend typecheck and production build run in CI.
- Production env template validation runs in CI.
- Docker Compose smoke runs in CI.
- Release runbook names the CI validation path.

## Design Notes

- CI uses GitHub-hosted runners and repo-owned workflow configuration.
- Backend tests use the existing Maven command; integration tests can use Docker
  from the hosted Ubuntu runner.
- Docker Compose smoke reuses `scripts/smoke/docker-compose-smoke.ps1`, keeping
  local and CI release checks aligned.
- Harness has no present CI provider registered, so this workflow is the durable
  repository-level CI proof.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 1 --platform 1`.

| Layer | Expected proof |
| --- | --- |
| Unit | Backend tests, frontend typecheck, frontend build. |
| Integration | Production env template check and Harness story verify. |
| E2E | Docker Compose smoke frontend-to-backend proxy check. |
| Platform | GitHub Actions workflow plus Docker Compose smoke. |

## Evidence

- `.\scripts\bin\harness-cli.exe query tools --capability ci --status present`
  returned no present providers.
- `powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template`
  passed.
- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run build` passed.
- `mvn -f backend/pom.xml test` passed.
- `powershell -ExecutionPolicy Bypass -File scripts/smoke/docker-compose-smoke.ps1 -SkipBuild`
  passed.
- `.\scripts\bin\harness-cli.exe story verify US-019` passed.
