# US-007 CV Files CMS And Public Download

## User Story

As an admin, I can upload and activate PDF CV files so recruiters can download
the correct active CV without logging in.

## Scope

- `cv_files` table and PDF byte storage.
- Admin list, upload, activate, archive, and soft-delete APIs.
- One ACTIVE CV per language and target role.
- Public active CV download API.
- Admin `/admin/cv-files` page and public `/cv` page.
- Download CV CTA on public home.

## Out Of Scope

- Audit log writes until E06.
- Object storage or media-library integration.
