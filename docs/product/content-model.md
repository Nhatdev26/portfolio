# Portfolio CMS Content Model

## Core Content

- Profile
- Profile Content
- Social Link
- Project
- Technical Note
- Technology
- Category
- Tag
- Skill Group

## Files And Media

- CV File
- Media Asset
- Media Usage

## Security And Audit

- User
- Refresh Token
- Audit Log
- Password Reset Token after MVP
- Status History after MVP

## Relationship Tables

- project_technologies
- note_technologies
- project_notes
- note_tags
- project_tags
- skill_group_technologies
- media_usages

## Public Visibility Rules

Public APIs may return only:

- Profile with ACTIVE status.
- Profile Content with ACTIVE status.
- Project with PUBLISHED status.
- Technical Note with PUBLISHED status.
- Technology with ACTIVE status.
- Category with ACTIVE status.
- Tag with ACTIVE status.
- CV File with ACTIVE status.
- Media Asset with READY status and PUBLIC visibility.

Public APIs must not return:

- DRAFT, SCHEDULED, UNPUBLISHED, ARCHIVED, DELETED, FAILED, or PRIVATE records.
- Password hashes.
- Refresh token hashes.
- Audit logs.
- Admin metadata.

## Data Integrity Rules

- Primary keys use BIGINT.
- Public URLs use slugs instead of database ids.
- Important entities use soft delete through `deleted_at` and `deleted_by`.
- Status fields use VARCHAR plus database checks where practical.
- Slugs use partial unique indexes where soft delete applies.
- Many-to-many relationships use explicit join tables.
- Audit old and new values use JSONB.
- One active CV exists per language and target role.
- One active profile content exists per profile and language.

