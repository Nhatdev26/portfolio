# US-022 - Public Skills Visual Polish And Home Section Order

## User Story

As a portfolio visitor, I want the public homepage to introduce the owner, show skills, and then show projects, with the skills page presented as grouped technical skill cards, so that the portfolio feels clear, polished, and easy to scan.

## Acceptance Criteria

- The public home page presents sections after the hero in this order: About, Skills, Projects, Blog, Contact.
- The public skills page uses grouped technical skill panels with a centered Technical Skills heading.
- Skill cards include a compact visual marker, technology name, and proficiency progress bar.
- The layout remains responsive on desktop and mobile widths.

## Implementation Notes

- Reordered `HomePage` sections without changing public routes or API contracts.
- Reworked `SkillsPage` rendering to map technologies into frontend, backend, and delivery categories.
- Added dark technical skill panel styling in the public stylesheet following the supplied visual reference.

## Verification

- `npm --prefix frontend run typecheck`
- `npm --prefix frontend run build`
- `docker compose up -d --build frontend`
- `powershell -ExecutionPolicy Bypass -File scripts\smoke\docker-compose-smoke.ps1 -SkipBuild`
- Browser verification at `http://localhost:5173/?v=us022` confirmed home section order: About, Skills, Projects, Blog, Contact.
- Browser verification at `http://localhost:5173/skills?v=us022` confirmed Technical Skills panels, skill cards, progress bars, and no console errors.
- Mobile browser verification at 375px confirmed no horizontal overflow on home or skills.
