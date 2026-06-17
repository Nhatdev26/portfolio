# Portfolio CMS Architecture

## Selected Stack

Backend:

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Hibernate
- Flyway
- PostgreSQL
- Maven

Frontend:

- React 19
- Vite 8
- TypeScript
- React Router
- TanStack Query
- React Hook Form
- Zod
- Fetch-based API client

Local platform:

- Docker Compose
- PostgreSQL container
- Backend container
- Frontend container

## Surfaces

Public Website:

- Routes live under the public React layout.
- Calls only `/public/*` APIs.
- Does not attach admin tokens.

Admin Dashboard:

- Routes live under the admin React layout.
- Login route is public.
- CMS routes require authenticated admin state once auth is implemented.
- Calls `/admin/*` APIs with an access token once auth is implemented.

Backend API:

- Public APIs expose only active or published content.
- Admin APIs expose CMS operations and require authentication once auth is
  implemented.
- Auth APIs issue and refresh tokens.

## Backend Boundaries

The backend follows a layered structure:

- Controllers receive HTTP requests and return DTO responses.
- Services own business rules.
- Repositories own persistence access.
- DTOs shape request and response contracts.
- Entities are not returned directly from APIs.
- Mappers convert between entities and DTOs.
- Global exception handling returns consistent API errors.

Package direction:

```text
common
auth
user
profile
project
note
technology
taxonomy
cv
media
audit
```

## Frontend Boundaries

The frontend keeps app structure by surface and feature:

```text
src/
  app/
  layouts/
  pages/
    public/
    admin/
  features/
  components/
  services/
  hooks/
  types/
  utils/
```

State rules:

- TanStack Query owns server state.
- React Hook Form owns form state.
- Zod owns input validation.
- Auth state will be added in the auth feature slice.
- Public API calls do not attach tokens.
- Admin API calls attach tokens only after login support exists.

## Validation Ladder

Phase 1 validation starts with structural checks because the current workspace
does not have Node, Java, Maven, Docker, or PostgreSQL on PATH.

Expected future ladder:

- Frontend quick: `npm install`, `npm run typecheck`, `npm run build`.
- Backend quick: `mvn test`.
- Integration: backend starts against PostgreSQL and Flyway migrates.
- E2E: browser smoke for public home, admin login, protected admin route.
- Platform: `docker compose up --build` starts database, backend, and frontend.

