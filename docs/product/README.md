# Product Docs

These files are the living product contract derived from `SPEC.md`.

- `overview.md`: product goal, roles, and MVP scope.
- `architecture.md`: selected stack, boundaries, and validation ladder.
- `content-model.md`: entities, visibility rules, and data integrity rules.
- `api-contract.md`: public/admin routes and API contract.
- `roadmap.md`: phase plan and foundation scope.

## Update Rule

When behavior changes:

1. Update the affected product doc.
2. Update or create the story packet.
3. Update durable proof status with `scripts/bin/harness-cli story add` or
   `scripts/bin/harness-cli story update`.
4. Record a decision if the change affects architecture, scope, risk, or a
   previously settled product rule.
