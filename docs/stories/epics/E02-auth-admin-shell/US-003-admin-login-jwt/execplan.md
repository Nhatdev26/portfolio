# Exec Plan

## Goal

Create the first working admin login and JWT session flow on top of the US-002
auth foundation.

## Scope

In scope:

- `/auth/login`, `/auth/refresh`, `/auth/logout`, and `/auth/me`.
- JWT access token signing and validation.
- Refresh-token hashing, rotation, and revocation.
- Backend route protection for `/auth/me` and `/admin/**`.
- Frontend protected admin route, login form submission, and sign out.
- Unit tests, Docker/API smoke, and browser smoke.

Out of scope:

- Audit writes.
- Password reset.
- CMS CRUD.
- Production cookie/CSRF hardening.

## Risk Classification

Risk flags:

- Auth.
- Authorization.
- Token/session security.
- Frontend route protection.
- Public contract.

Hard gates:

- Auth.
- Authorization.
- Weak proof.

## Work Phases

1. Discovery.
2. Design.
3. Backend auth implementation.
4. Frontend login/session implementation.
5. Unit and build validation.
6. Docker/API/browser validation.
7. Harness and docs update.

## Stop Conditions

Pause for human confirmation if:

- The API path contract changes.
- Production-grade cookie/session hardening becomes required in this story.
- Docker/browser proof cannot be run.
