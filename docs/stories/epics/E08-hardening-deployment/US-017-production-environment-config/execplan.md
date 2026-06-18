# US-017 Production Environment Config Exec Plan

## Goal

Separate production environment configuration from local defaults and provide a
repeatable guardrail for secret and origin safety.

## Scope

In scope:

- Add `.env.production.example`.
- Add a production environment check script.
- Document production variables and storage boundary.
- Preserve local Docker Compose ergonomics.

Out of scope:

- Cloud deployment automation.
- Object storage migration.
- Secret manager integration.

## Risk Classification

Risk flags:

- Audit/security.
- Existing behavior.
- Public contracts.

Hard gates:

- Audit/security.

## Work Phases

1. Discovery.
2. Environment template design.
3. Validation script implementation.
4. Documentation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- Production behavior requires a hosting provider decision.
- Existing local development commands need to be broken.
- The storage boundary changes from PostgreSQL to filesystem/object storage.
