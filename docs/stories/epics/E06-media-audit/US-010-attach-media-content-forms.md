# US-010 Attach Media In Content Forms

## Status

in_progress

## Lane

normal

## Product Contract

Admins can attach READY media assets to Project and Blog content from the
content editing forms. Public Project and Blog pages render selected public
media when available, while keeping existing media delete protection and usage
tracking intact.

## Relevant Product Docs

- `docs/product/api-contract.md`
- `docs/product/content-model.md`
- `docs/stories/epics/E06-media-audit/US-009-media-library/design.md`
- `docs/stories/epics/E05-projects-notes/US-006-projects-notes-cms/design.md`

## Acceptance Criteria

- Project form can select one cover image and optional screenshots from READY media.
- Blog form can select one content image or diagram from READY media.
- Selected media previews show title, alt text, visibility, usage type, and image preview when possible.
- Saving content attaches/detaches media usages for the saved entity.
- Public Project and Blog APIs include media summaries scoped to the entity.
- Public pages render only READY + PUBLIC media through the public media content endpoint.
- Existing media delete protection continues to block deleting attached media.

## Design Notes

- Commands: content save remains the authoritative Project/Note mutation; media usage updates run after the entity has an id.
- Queries: add media usage lookup by `entityType` and `entityId`.
- API: expose entity media summaries on project and note responses; reuse existing admin attach/detach usage endpoints.
- Tables: reuse `media_usages`; no migration expected.
- Domain rules: only READY assets can be attached; public render requires READY + PUBLIC.
- UI surfaces: admin Project form, admin Blog form, public Project list/detail, public Blog list/detail.

## Validation

When updating durable proof status, use numeric booleans:
`scripts/bin/harness-cli story update --id US-010 --unit 1 --integration 1 --e2e 0 --platform 0`.

| Layer | Expected proof |
| --- | --- |
| Unit | Backend service tests for entity media summaries and usage synchronization behavior where practical. |
| Integration | `mvn test`, frontend `npm run typecheck`, frontend `npm run build`. |
| E2E | Browser smoke for admin Project/Blog media picker and public rendering. |
| Platform | Docker build/up plus desktop and 375px mobile no horizontal overflow. |
| Release | PR body records validation evidence. |

## Harness Delta

None expected.

## Evidence

- Backend `mvn test` passed 39/39 after adding media summaries to content responses.
- Frontend `npm run typecheck` passed after wiring media picker and public rendering.
- Frontend `npm run build` passed after wiring media picker and public rendering.
