# Portfolio CMS Product Overview

## Product

Portfolio CMS is a full-stack content management system for a backend-focused
developer portfolio.

The system has two user-facing surfaces:

- Public Portfolio Website: open to visitors without login.
- Admin Dashboard: protected CMS interface for the portfolio owner.

## Product Goal

The product should demonstrate practical full-stack capability:

- Java and Spring Boot backend development.
- PostgreSQL relational schema design.
- REST API design with clear public and admin boundaries.
- JWT authentication and refresh-token flow.
- React admin dashboard and public portfolio site.
- Content workflow for draft, published, archived, and deleted content.
- Media upload, CV management, audit logging, SEO, and Docker deployment.

## Roles

Public Visitor:

- Does not authenticate.
- Can view public profile, projects, notes, technologies, and CV.
- Cannot view draft/admin data or mutate any content.

Admin:

- Must authenticate.
- Can manage profile, social links, CVs, projects, notes, technologies,
  categories, tags, skill groups, media assets, and audit logs.

Future roles are prepared conceptually but not implemented in MVP:

- ADMIN
- EDITOR
- REVIEWER
- VIEWER

## MVP Scope

The MVP is complete when:

- Admin can log in from the React Admin Dashboard.
- Admin can manage profile, social links, CV, taxonomy, technologies, projects,
  notes, media, and audit logs.
- Public visitors can browse published portfolio content without login.
- Draft, unpublished, archived, deleted, private, and admin-only data never
  leak through public APIs.
- The full system can run locally through Docker Compose.

