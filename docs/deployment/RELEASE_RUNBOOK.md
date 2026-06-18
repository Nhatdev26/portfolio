# MVP Release Runbook

This runbook is the final local release-readiness path for the Portfolio CMS
MVP.

## Preconditions

- Docker Desktop is running.
- Node.js and npm are installed.
- Java 21 and Maven are installed.
- `.env.production` is created from `.env.production.example` for any real
  deployment.
- `ADMIN_SEED_ENABLED=true` is used only for the first production boot, then set
  back to `false`.

## Local Verification

Run the backend suite:

```powershell
mvn -f backend/pom.xml test
```

Run frontend checks:

```powershell
npm --prefix frontend run typecheck
npm --prefix frontend run build
```

Validate production environment shape:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template
```

Run the full Docker Compose smoke:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke/docker-compose-smoke.ps1
```

For repeated local checks after images are already built:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke/docker-compose-smoke.ps1 -SkipBuild
```

## Manual Smoke

After the stack is running:

- Open `http://localhost:5173/`.
- Confirm the public portfolio shell loads.
- Open `http://localhost:5173/api/health`.
- Confirm it returns backend health.
- Open `http://localhost:5173/admin/login`.
- Log in only if a local/dev admin seed has been enabled.

## Release Notes

- Public visitors do not need login.
- Admin APIs are protected by bearer auth.
- Draft/unpublished content is not public.
- CV/media bytes are stored in PostgreSQL for this MVP.
- Real secrets must live outside the repository.
