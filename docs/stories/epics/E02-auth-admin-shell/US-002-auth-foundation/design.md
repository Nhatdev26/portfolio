# Design

## Domain Model

User:

- Represents CMS/admin accounts only.
- Has email, password hash, role, status, timestamps, and soft-delete fields.
- Role values: `ADMIN`, `EDITOR`, `REVIEWER`, `VIEWER`.
- Status values: `ACTIVE`, `DISABLED`, `LOCKED`.

Refresh Token:

- Belongs to a user.
- Stores only `token_hash`, never the raw token.
- Carries expiry and revocation timestamps for future auth stories.

## Application Flow

First-admin seeding:

1. Read `ADMIN_SEED_ENABLED`, `ADMIN_SEED_EMAIL`, and `ADMIN_SEED_PASSWORD`.
2. If seed is disabled, do nothing.
3. If seed is enabled and no user exists for that email, hash the password with
   BCrypt and create an ACTIVE ADMIN user.
4. If the user already exists, do nothing.

## Interface Contract

This story does not add public HTTP auth endpoints.

Temporary security chain:

- Adds Spring Security so password hashing and later auth work have the correct
  foundation.
- Keeps requests permitted in this story to avoid silently changing Phase 1
  public behavior before login/JWT support exists.

## Data Model

Migration `V2__auth_foundation.sql` creates:

- `users`
- `refresh_tokens`

Rules:

- Email is unique for non-deleted users through a partial unique index on
  `lower(email)`.
- Important status and role fields use CHECK constraints.
- Soft delete uses `deleted_at` and `deleted_by`.
- Refresh tokens store hashed tokens only.

## UI / Platform Impact

No UI change is included. Existing public and admin shell routes should still
render after Spring Security is added.

## Observability

The seeder logs whether it skipped or created the first admin. It must not log
passwords or hashes.

## Alternatives Considered

1. Add login/JWT in the same story. Rejected to keep the high-risk auth area
   reviewable and small.
2. Seed a hard-coded admin. Rejected because secrets must come from
   environment variables.
3. Store raw refresh tokens. Rejected because the product security contract
   requires token hashes only.

