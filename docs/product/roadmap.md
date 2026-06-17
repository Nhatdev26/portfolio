# Portfolio CMS Roadmap

## Phase 0 - Product, Architecture, Planning

Goal: convert the supplied specification into living product docs, story
packets, validation expectations, and durable decisions.

## Phase 1 - Full-stack Project Foundation

Goal: create backend, frontend, database, Docker, and project structure.

Expected output:

- Spring Boot backend starts locally.
- Health endpoint works.
- PostgreSQL connection is configured through environment variables.
- Flyway migration folder exists.
- React Vite frontend starts locally.
- React Router, TanStack Query, layouts, API client, and NotFound page exist.
- Docker Compose describes PostgreSQL, backend, and frontend services.

## Phase 2 - Authentication And Admin Shell

Goal: implement admin user authentication, JWT access tokens, refresh tokens,
and protected admin shell.

## Phase 3 - Profile, Social Links, CV

Goal: implement the first full vertical CMS slice from database to admin UI and
public display.

## Phase 4 - Taxonomy, Technology, Skill Groups

Goal: add reusable classification and technology records.

## Phase 5 - Projects And Technical Notes CMS

Goal: implement portfolio project and note authoring workflows.

## Phase 6 - Media Asset And Audit Log

Goal: implement media upload, usage tracking, delete protection, and audit log
visibility.

## Phase 7 - Public Portfolio Website

Goal: publish profile, projects, notes, technologies, CV, and contact sections
without login.

## Phase 8 - Testing, Security, SEO, Deployment

Goal: harden critical workflows, public/admin separation, responsive design,
SEO metadata, and Docker deployment.

