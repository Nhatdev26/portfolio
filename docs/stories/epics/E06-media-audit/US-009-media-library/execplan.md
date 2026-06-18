# Exec Plan

## Goal

Implement the E06 media library vertical slice with upload metadata, admin
library UI, public visibility enforcement, audit events, and delete protection.

## Scope

In scope:

- Media asset and usage schema.
- Admin media upload, list, update, delete, content, attach, and detach APIs.
- Public READY + PUBLIC content endpoint.
- Admin `/admin/media` library UI.
- Backend unit tests, frontend typecheck/build, Docker/API/browser smoke.
- Product docs, decision record, matrix, and validation evidence.

Out of scope:

- Project/note/profile/technology form media pickers.
- Image resizing, variants, external object storage, and CDN behavior.

## Risk Classification

Risk flags:

- Data model.
- Audit/security.
- Public contracts.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- Data migration.
- Audit/security.
- Public API shape.

## Work Phases

1. Discovery.
2. Design.
3. Validation planning.
4. Implementation.
5. Verification.
6. Harness update.

## Stop Conditions

Pause for human confirmation if:

- The storage boundary must switch to external object storage.
- Existing published content needs automatic media attachments in this story.
- Validation requirements need to be weakened.
