# US-005 Taxonomy Technology Skill Groups

## User Story

As a portfolio CMS admin, I can manage categories, tags, technologies, and skill
groups so that public content can be classified and technology pages can show
only active, curated information.

## Scope

- Admin CRUD-style save/archive endpoints for categories, tags, technologies,
  and skill groups.
- Slug uniqueness among non-deleted records.
- Active-only public lists and technology detail.
- Skill groups attach only active technologies.
- Admin UI routes for categories, tags, technologies, and skill groups.

## Out Of Scope

- Audit log writes; deferred to E06.
- Per-group technology ordering beyond the active technology set.
