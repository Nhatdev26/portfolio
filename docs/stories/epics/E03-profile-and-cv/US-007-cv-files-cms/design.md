# Design

## Backend

- Store PDF bytes directly in `cv_files.file_data`.
- `CvFileService` validates PDF content type, `.pdf` filename, and maximum
  size.
- Activation is transactional and deactivates competing active CVs for the
  same language and target role.
- Public download streams a `ByteArrayResource` with PDF content type and a
  content-disposition filename.

## Frontend

- Admin page uses a compact upload form plus a data table.
- Active CVs are visually highlighted with an ACTIVE status chip.
- Public `/cv` provides a focused download experience and a friendly unavailable
  state.
- Home page includes a Download CV CTA.

## UX Notes

The UI follows the `ui-ux-pro-max` CMS direction: clear labels, loading/error
states, semantic controls, no emoji icons, stable tables, and touch-friendly
buttons.
