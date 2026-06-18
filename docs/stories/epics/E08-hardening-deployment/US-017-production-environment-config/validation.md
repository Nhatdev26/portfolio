# US-017 Production Environment Config Validation

## Proof Strategy

Validate the production template with a script that rejects missing keys,
unsafe local defaults, non-HTTPS production origins, and undersized JWT secrets.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Not required. |
| Integration | Production env template parser and validation script. |
| E2E | Not required. |
| Platform | Compose smoke remains covered by US-016/US-018. |
| Performance | Not required. |
| Logs/Audit | Not required. |

## Fixtures

- `.env.production.example`

## Commands

```text
powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template
```

## Acceptance Evidence

- `powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template`
  passed.
- `.\scripts\bin\harness-cli.exe story verify US-017` passed.
- `mvn -f backend/pom.xml test` passed: 48 tests, 0 failures.
- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run build` passed.
