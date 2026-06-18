# US-017 Production Environment Config Design

## Domain Model

No domain model change.

## Application Flow

Production operators copy `.env.production.example` to `.env.production`, replace
placeholder values, run `production-env-check.ps1`, then start the Docker stack
with the production env file.

## Interface Contract

No HTTP API contract change.

## Data Model

No migration. `FILE_STORAGE_PATH` is exposed as a configuration slot only; the
current MVP continues storing CV/media bytes in PostgreSQL.

## UI / Platform Impact

Frontend containers continue supporting runtime `PORTFOLIO_API_BASE_URL`.
Production docs require HTTPS public origins.

## Observability

No new application logs or audit records.

## Alternatives Considered

1. Remove all local Docker Compose defaults. Rejected because it would make
   learning/local setup unnecessarily brittle.
2. Add a cloud-provider-specific deploy path. Deferred until a provider is
   selected.
