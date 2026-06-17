# Design

## Backend

- Flyway V4 creates taxonomy tables and relationship tables.
- `TaxonomyService` enforces slug uniqueness, archive/soft-delete behavior, and
  active-only public visibility.
- Public routes expose `/public/categories`, `/public/tags`,
  `/public/technologies`, `/public/technologies/{slug}`, and
  `/public/skill-groups`.

## Frontend

- `TaxonomyPage` reuses one dense CMS layout for the four taxonomy sections.
- Forms use labels, status selects, numeric order inputs, and checkbox grids for
  skill-group technology assignment.
- Public technology detail renders the active technology description and usage.

## UX Notes

The frontend follows the `ui-ux-pro-max` direction selected for this slice:
quiet SaaS/CMS layout, clear form grouping, active navigation, empty/error
states, and accessible labels.
