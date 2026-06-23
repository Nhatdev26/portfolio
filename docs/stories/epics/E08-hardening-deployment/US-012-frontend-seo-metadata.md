# US-012 - Frontend SEO Metadata

## Summary

Add SEO metadata management to the React public portfolio so shared links,
browser titles, descriptions, canonical URLs, and crawl intent are consistent
across public and admin routes.

## Source

- `SPEC.md` Phase 8, US-8.5 - Frontend SEO Basics.
- Product direction: public site focuses on introduction, projects, skills, and
  blog.

## Acceptance Criteria

- Home, About, Projects, Skills, Blog, Project Detail, Note Detail, Technology
  Detail, and 404 routes set a page-specific document title.
- Public routes set meta description, canonical URL, Open Graph title,
  description, URL, type, and Twitter summary metadata.
- Project and Note detail pages use CMS SEO title/description when available,
  with title/summary/excerpt fallbacks.
- Admin layout and login pages set `robots` to `noindex,nofollow`.
- `frontend/index.html` keeps a useful fallback title and description before the
  React app hydrates.
- Implementation does not add a new runtime dependency unless needed.

## Non-goals

- Server-side rendering or prerendering.
- Sitemap generation.
- Backend SEO API changes.
- OG image upload workflow.

## Validation Plan

- `npm --prefix frontend run typecheck`
- `npm --prefix frontend run build`
- Browser smoke on public and admin routes:
  - Verify document title, description, canonical, OG/Twitter metadata.
  - Verify admin/login robots are `noindex,nofollow`.
  - Verify no console warnings/errors or horizontal overflow on desktop/mobile.
