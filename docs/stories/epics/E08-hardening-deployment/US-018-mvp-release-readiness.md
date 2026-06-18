# US-018 MVP Release Readiness

## Status

implemented

## Lane

normal

## Product Contract

The MVP has a release-readiness runbook and a final local proof path that ties
together backend tests, frontend build, production env validation, and Docker
Compose smoke.

## Relevant Product Docs

- `SPEC.md`
- `docs/deployment/ENVIRONMENT.md`
- `docs/deployment/RELEASE_RUNBOOK.md`

## Acceptance Criteria

- Release runbook exists.
- Production env template validation is documented.
- Backend validation command is documented.
- Frontend validation commands are documented.
- Docker Compose smoke command is documented.
- Phase 8 proof records identify remaining non-goals.

## Design Notes

- US-018 is a local MVP readiness packet because `SPEC.md` ends at US-8.9.
- It does not introduce a cloud provider, CI workflow, or release artifact
  registry.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id <id> --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Backend test suite and frontend typecheck/build. |
| Integration | Harness story verify. |
| E2E | Docker Compose smoke frontend-to-backend proxy check. |
| Platform | Docker Compose smoke. |
| Release | Runbook plus production env template validation. |

## Harness Delta

No Harness policy change.

## Evidence

- `mvn -f backend/pom.xml test` passed: 48 tests, 0 failures.
- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run build` passed.
- `powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template`
  passed.
- `.\scripts\bin\harness-cli.exe story verify US-017` passed.
- `.\scripts\bin\harness-cli.exe story verify US-018` passed.
- `.\scripts\bin\harness-cli.exe query tools --capability deploy-verification --status present`
  returned no present providers, so `scripts/smoke/docker-compose-smoke.ps1`
  is the local platform proof.
