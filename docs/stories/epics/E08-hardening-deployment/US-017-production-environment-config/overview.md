# US-017 Production Environment Config Overview

## Current Behavior

Local Docker Compose runs with convenient development defaults. Production
operators need a separate template and validation command so real deployments
do not accidentally reuse development secrets or localhost-only origins.

## Target Behavior

Production configuration is separated from local defaults and can be validated
before deployment.

## Affected Users

- System owner.
- Admin.
- Developer/operator.

## Affected Product Docs

- `SPEC.md`
- `docs/product/roadmap.md`
- `docs/deployment/ENVIRONMENT.md`

## Non-Goals

- Introduce a new hosting provider.
- Move media/CV bytes out of PostgreSQL.
- Remove local development defaults.
