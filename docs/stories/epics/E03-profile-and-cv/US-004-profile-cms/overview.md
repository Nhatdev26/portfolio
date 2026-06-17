# Profile CMS Vertical Slice

## Current Behavior

The public site has placeholder profile routes and the admin shell has a
Profile navigation item, but there is no persisted profile content, no social
link management, and no public profile API.

## Target Behavior

- Admin can create or update the portfolio profile from `/admin/profile`.
- Profile stores identity fields, role, direction, tech focus, and status.
- Admin can maintain EN and VI profile content blocks.
- Admin can add, edit, deactivate, or remove social links.
- Public `/` and `/about` render ACTIVE profile content when it exists.
- Public `/public/profile?language=EN|VI` returns only ACTIVE profile and
  ACTIVE localized content.
- Public profile responses include only ACTIVE social links.

## Affected Users

- Admin.
- Public visitor.
- Future developer implementing CV and project/notes vertical slices.

## Affected Product Docs

- `docs/product/api-contract.md`
- `docs/product/architecture.md`
- `docs/product/content-model.md`
- `docs/TEST_MATRIX.md`

## Non-Goals

- CV upload and one-active-CV rule.
- Audit log writes for profile changes.
- Rich media/avatar management.
- Full SEO metadata rendering in React document head.
