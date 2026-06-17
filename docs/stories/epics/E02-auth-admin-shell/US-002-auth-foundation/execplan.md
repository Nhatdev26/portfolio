# Exec Plan

## Goal

Create the backend auth foundation for CMS admin accounts without exposing login
or protected-route behavior yet.

## Scope

In scope:

- Migration for `users` and `refresh_tokens`.
- User and refresh-token entities.
- User role and status enums.
- User repository.
- BCrypt password encoder.
- Environment-driven first-admin seeder.
- Unit tests and Docker/Flyway proof.
- Harness story, matrix, and decision updates.

Out of scope:

- JWT access tokens.
- Auth controller endpoints.
- Refresh-token rotation.
- Frontend login submission.
- Route protection.

## Risk Classification

Risk flags:

- Auth.
- Authorization.
- Data model.
- Audit/security.
- Public contracts.
- Weak proof.

Hard gates:

- Auth.
- Authorization.
- Audit/security.

## Work Phases

1. Create high-risk Harness story and durable decision.
2. Add auth schema migration.
3. Add backend auth/user model and seed support.
4. Add unit tests.
5. Run backend and platform validation.
6. Update Harness matrix and trace.
7. Commit and push branch.

## Stop Conditions

Pause for human confirmation if:

- Login/JWT scope needs to be included in this same branch.
- A destructive migration or data loss risk appears.
- Validation requirements need to be weakened.
- Remote push requires credentials not available in the current session.

