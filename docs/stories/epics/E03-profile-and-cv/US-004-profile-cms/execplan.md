# Exec Plan

## Goal

Create the first working profile CMS vertical slice from database to public
rendering.

## Scope

In scope:

- Profile, profile content, and social link schema.
- Backend admin read/save API.
- Backend public profile API.
- Frontend admin profile editor.
- Frontend public home/about rendering from active profile content.
- Backend unit tests, frontend typecheck/build, Docker/API smoke, and browser
  smoke.

Out of scope:

- CV file upload.
- Audit log writes.
- Rich text/markdown editing.
- Image/avatar upload.

## Risk Classification

Risk flags:

- Public content visibility.
- Admin authorization.
- Data integrity.
- Frontend form state.

Hard gates:

- Authorization.
- Public/private data split.
- Weak proof.

## Work Phases

1. Discovery.
2. Design and API path decision.
3. Backend schema/domain/API implementation.
4. Frontend admin/public implementation.
5. Unit and build validation.
6. Docker/API/browser validation.
7. Harness and docs update.
8. Commit and push branch.

## Stop Conditions

Pause for human confirmation if:

- The API path contract changes again.
- CV upload becomes required in this story.
- Docker/browser proof cannot be run.
