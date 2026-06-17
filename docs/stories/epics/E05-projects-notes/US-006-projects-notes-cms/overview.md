# US-006 Projects And Technical Notes CMS

## User Story

As a portfolio CMS admin, I can draft and publish projects and technical notes
with taxonomy relationships so that public visitors see only published,
structured portfolio content.

## Scope

- Admin create/update/archive/delete APIs for projects and technical notes.
- Published-only public project and note list/detail APIs.
- Slug uniqueness per language among non-deleted records.
- Publish validation requiring project SEO and at least one active technology.
- Publish validation requiring note SEO and an active category.
- Admin list/new/edit routes and public list/detail routes.

## Out Of Scope

- Full markdown renderer package selection.
- Media embeds and audit log writes.
