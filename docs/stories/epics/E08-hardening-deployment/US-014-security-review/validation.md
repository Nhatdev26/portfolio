# US-014 Security Review Validation

## Proof Strategy

Use focused web-security regression tests plus the full backend test suite.
Existing service tests continue to cover token hashing, auth service behavior,
media/CV upload validation, and audit redaction.

## Test Plan

| Layer | Cases |
| --- | --- |
| Unit | Existing auth, media, CV, audit service tests. |
| Integration | `SecurityBoundaryTests` with real `SecurityConfig` and JWT filter in web slice. |
| E2E | Not required for this backend security review. |
| Platform | Docker backend build/up if code behavior changes beyond tests. |
| Performance | Not required. |
| Logs/Audit | Existing audit redaction tests plus code review of audit payloads. |

## Fixtures

- Mocked services for web-security route tests.
- Configured local CORS origin: `http://localhost:5173`.

## Commands

```text
mvn -f backend/pom.xml -Dtest=SecurityBoundaryTests test
mvn -f backend/pom.xml test
.\scripts\bin\harness-cli.exe story verify US-014
```

## Acceptance Evidence

- `mvn -f backend/pom.xml -Dtest=SecurityBoundaryTests test` passed: 6
  tests, 0 failures.
- `mvn -f backend/pom.xml test` passed: 44 tests, 0 failures.
- `.\scripts\bin\harness-cli.exe story verify US-014` passed.
- `.\scripts\bin\harness-cli.exe query tools --capability security-scan
  --status present` returned no registered present providers, so external
  security scanning was a clean skip for this story.
