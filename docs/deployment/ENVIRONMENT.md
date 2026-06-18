# Production Environment

This project keeps local development defaults in `.env.example` and production
placeholders in `.env.production.example`.

Do not commit real production `.env` files. The repository `.gitignore` ignores
`.env` and `.env.*`, while explicitly allowing the example templates.

## Required Variables

| Variable | Purpose | Production guidance |
| --- | --- | --- |
| `POSTGRES_DB` | PostgreSQL database name. | Use a stable database name. |
| `POSTGRES_USER` | PostgreSQL bootstrap user. | Use an app-specific user. |
| `POSTGRES_PASSWORD` | PostgreSQL bootstrap password. | Use a strong generated secret. |
| `DB_URL` | Backend JDBC URL. | Point at the production database host. |
| `DB_USERNAME` | Backend database username. | Match the production app user. |
| `DB_PASSWORD` | Backend database password. | Use a strong generated secret. |
| `CORS_ALLOWED_ORIGINS` | Browser origins allowed to call the backend. | Use only HTTPS production origins. |
| `PORTFOLIO_API_BASE_URL` | Runtime API base URL for the frontend container. | Use the public HTTPS origin or leave blank for same-origin proxy deployments. |
| `AUTH_JWT_SECRET` | HS256 JWT signing secret. | Use at least 32 random bytes; 64+ characters is preferred. |
| `AUTH_ACCESS_TOKEN_MINUTES` | Access token lifetime. | Keep short, for example 15 minutes. |
| `AUTH_REFRESH_TOKEN_DAYS` | Refresh token lifetime. | Use the expected admin session window. |
| `ADMIN_SEED_ENABLED` | First-admin seed switch. | Use `true` only for first boot, then switch to `false`. |
| `ADMIN_SEED_EMAIL` | First admin email. | Use the real owner/admin email. |
| `ADMIN_SEED_PASSWORD` | First admin password. | Use a temporary strong password and rotate after login. |
| `FILE_STORAGE_PATH` | Future filesystem/object-storage path. | Current MVP stores CV/media bytes in PostgreSQL; keep the variable configured for future migration. |

## Validation

Check the production template:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production.example -Template
```

Check a real production file before deploy:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke/production-env-check.ps1 -Path .env.production
```

The real-file mode fails when a value still uses `replace-with-*`, local dev
defaults, an undersized JWT secret, or non-HTTPS production browser origins.

## Storage Boundary

CV files and media assets are stored in PostgreSQL for the MVP, per
`docs/decisions/0013-cv-file-storage-boundary.md` and
`docs/decisions/0015-media-storage-boundary.md`.

`FILE_STORAGE_PATH` exists as a production configuration slot for a later
filesystem/object-storage migration. It is not mounted or used by default in the
current Docker Compose stack.
