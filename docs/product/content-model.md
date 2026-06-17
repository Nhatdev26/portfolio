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

User records are CMS/admin accounts only. Public visitors are never stored in
`users`.

Refresh tokens store token hashes only. Raw refresh tokens must not be
persisted.

Refresh-token rotation revokes the old token and persists a new token hash.
Logout revokes the presented refresh token.

Audit logs are immutable product records for important CMS actions. They store
actor id/email, action, entity type/id/title, result, timestamp, and optional
old/new JSONB values. Passwords, tokens, secrets, and authorization values must
be redacted before persistence.

Profile records own identity fields and publication status. Profile Content
records own localized copy for EN and VI. Social Link records own platform,
label, URL, display order, and active/inactive state.

Categories and tags use ACTIVE/ARCHIVED status, slug uniqueness among
non-deleted records, display order, and soft delete.

Technologies use ACTIVE/ARCHIVED status, type, description, usage notes,
core/non-core flag, display order, slug uniqueness among non-deleted records,
and soft delete.

Skill groups use ACTIVE/ARCHIVED status and attach active technologies through
`skill_group_technologies`.

Projects own localized portfolio case-study content, project lifecycle status,
content publish status, SEO fields, source/demo links, and relationships to
technologies, tags, and related notes. A project must have at least one active
technology and SEO title/description before it can be published.

Technical notes own localized markdown-like content, category, reading minutes,
SEO fields, and relationships to technologies and tags. A note must have an
active category and SEO title/description before it can be published.

CV files own PDF bytes, language, target role, version, file metadata, upload
time, activation time, and status. Uploads are DRAFT by default. Activation
marks the selected CV ACTIVE and archives any other ACTIVE CV for the same
language and target role.

Media assets own uploaded bytes, original filename, content type, file size,
title, alt text, caption, READY/DELETED/FAILED status, PUBLIC/PRIVATE
visibility, upload time, and soft-delete timestamp. Media usages attach READY
assets to PROJECT, TECHNICAL_NOTE, TECHNOLOGY, or PROFILE entities with a
specific usage type. Used media cannot be deleted until usages are detached.
Project and Technical Note public responses expose attached media summaries only
when the media asset is READY and PUBLIC.

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
- One active profile exists at a time.
- One active profile content exists per profile and language.
- Social links require `mailto:` for EMAIL and `http` or `https` for other
  platforms.
- Project slugs are unique per language among non-deleted projects.
- Technical note slugs are unique per language among non-deleted notes.
- Category, tag, technology, and skill-group slugs are unique among non-deleted
  records.
- CV uploads accept only PDF files up to 5 MB.
- Media uploads accept JPEG, PNG, WebP, GIF, SVG, and PDF files up to 10 MB.
- Media assets with usage records are delete-protected.
- Audit logs are read-only and never exposed by public APIs.
