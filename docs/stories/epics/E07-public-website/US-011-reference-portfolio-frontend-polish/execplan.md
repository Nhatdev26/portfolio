# Exec Plan

## Goal

Make the frontend feel closer to the referenced developer portfolio while
preserving data contracts and CMS behavior.

## Scope

In scope:

- Public layout, home, about, project, skill, blog, and technology page polish.
- CSS design tokens, animation, hover/focus states, and responsive treatment.
- Browser smoke on public and admin routes.
- Story and matrix proof updates.

Out of scope:

- Backend or schema changes.
- New media picker behavior.
- Copying reference site content or assets.

## Risk Classification

Risk flags:

- Cross-platform.
- Existing behavior.
- Weak proof.
- Multi-domain.

Hard gates:

- None.

## Work Phases

1. Inspect reference site.
2. Inspect current frontend routes.
3. Implement public shell/page polish.
4. Validate typecheck, build, and browser smoke.
5. Update Harness records.

## Stop Conditions

Pause for human confirmation if:

- The request changes from visual inspiration to exact cloning.
- Backend or content-model changes become necessary.
- Validation cannot be run.
