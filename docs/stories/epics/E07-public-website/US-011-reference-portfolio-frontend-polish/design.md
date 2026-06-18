# Design

## Domain Model

No domain model changes.

## Application Flow

No API flow changes. Existing public profile, project, note, technology, and
health queries are reused.

## Interface Contract

Frontend routes remain unchanged:

- `/`
- `/about`
- `/projects`
- `/projects/:slug`
- `/skills`
- `/notes`
- `/notes/:slug`
- `/technologies/:slug`

## Data Model

No data model changes.

## UI / Platform Impact

Reference patterns to adapt:

- Fixed glass public navigation on dark background.
- Cyan/blue gradient brand mark and headline treatment.
- Rounded pill CTAs and hover underlines.
- Elevated dark cards with subtle border glow.
- Richer home page sections for focus areas, selected work, skills, blog, and contact.
- Mobile bottom navigation.
- Reduced-motion fallback for animations.

Admin impact is limited to shared color tokens, focus states, hover polish, and
visual consistency.

## Observability

Browser smoke must check console warnings/errors and horizontal overflow across
desktop and mobile.

## Alternatives Considered

1. Rewrite the app with Tailwind/Framer Motion like the reference. Rejected to
   avoid dependency churn and to keep the existing CSS architecture.
2. Copy reference assets. Rejected; only visual patterns are reused.
