# Overview

## Current Behavior

The admin route `/admin/media` is a placeholder. The product model names Media
Asset and Media Usage, but there is no schema, API, upload workflow, metadata
editing, usage visibility, or delete protection yet.

## Target Behavior

Admins can upload media assets, list them in the media library, edit title,
alt text, caption, and visibility, inspect usage records, and delete only media
that is not attached to content. Public media bytes are available only for
READY assets with PUBLIC visibility.

## Affected Users

- CMS admin.
- Public visitor indirectly, through public media visibility rules.

## Affected Product Docs

- `docs/product/api-contract.md`
- `docs/product/content-model.md`
- `docs/TEST_MATRIX.md`

## Non-Goals

- Direct media picker integration inside project, note, technology, and profile
  forms. That remains US-010.
- External object storage, image transformation, or CDN integration.
- Public listing of media assets.
