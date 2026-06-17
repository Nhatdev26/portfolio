# Design

## Backend

- `Project` and `TechnicalNote` entities use shared content language/status
  enums.
- `ContentService` owns relationship loading, publish validation, slug
  uniqueness, and public mapping.
- Public mappers filter inactive taxonomy records and unpublished related notes.

## Frontend

- Project and note list pages show status, taxonomy relationships, and archive
  actions.
- Project and note form pages group core content, relations, SEO, and case-study
  fields.
- Public pages render published lists, detail pages, technology chips, and
  related note links.

## UX Notes

The CMS pages favor dense but readable operational UI: restrained panels, data
tables, semantic labels, visible loading/empty/error states, and responsive
grids. Public pages use repeated cards and concise chips for scanability.
