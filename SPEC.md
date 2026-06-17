# Full-stack Product & Engineering Specification

# Portfolio CMS System

# Version 2.0

## 0. Mục đích tài liệu

Tài liệu này là bản spec hợp nhất cho toàn bộ hệ thống **Portfolio CMS System**, bao gồm cả:

```txt id="azya36"
Backend
Frontend
Database
Admin Dashboard
Public Portfolio Website
Authentication
Authorization
Media Upload
Audit Log
SEO
Deployment
Testing
```

Hệ thống được xây theo hướng **learning-first** để người triển khai học được cách làm sản phẩm full-stack thực tế:

```txt id="qk0fkq"
Product thinking
Entity modeling
Database design
Spring Boot backend
ReactJS frontend
REST API design
Authentication with JWT
Admin Dashboard
Public Website
Content workflow
File upload
Audit log
SEO basics
Deployment
Testing
```

---

# 1. Product Overview

## 1.1 Product Name

```txt id="77dejc"
Portfolio CMS System
```

## 1.2 Product Vision

Xây dựng một hệ thống Portfolio CMS chuyên nghiệp cho một Backend Developer tập trung vào Java / Spring Boot, cho phép Admin quản lý profile, project, technical notes, technologies, CV, media và audit logs thông qua Admin Dashboard, đồng thời cung cấp Public Portfolio Website để nhà tuyển dụng và người xem truy cập tự do.

## 1.3 Main Goal

Hệ thống phải chứng minh được:

```txt id="c7a682"
Tôi biết xây backend system thực tế bằng Java Spring Boot.
Tôi biết thiết kế database quan hệ với PostgreSQL.
Tôi biết làm authentication, authorization, JWT và refresh token.
Tôi biết xây Admin Dashboard bằng ReactJS.
Tôi biết xây Public Portfolio Website bằng ReactJS.
Tôi biết thiết kế REST API rõ ràng.
Tôi biết quản lý content workflow: draft, published, archived.
Tôi biết upload và quản lý file/media.
Tôi biết ghi Audit Log.
Tôi biết tách public data và admin data an toàn.
```

## 1.4 Product Positioning

```txt id="a0ef5d"
A full-stack Portfolio CMS System built with Java Spring Boot, PostgreSQL and ReactJS, designed to manage backend developer portfolio content through a secure admin dashboard and publish selected content to a public portfolio website.
```

---

# 2. User Roles

## 2.1 Public Visitor

Public Visitor là người xem website portfolio công khai.

Bao gồm:

```txt id="1ath04"
Nhà tuyển dụng
HR
Technical interviewer
Engineering manager
Đối tác
Khách hàng tiềm năng
Người đọc Technical Notes
```

Public Visitor không cần đăng nhập.

Public Visitor có thể:

```txt id="xnd1to"
Xem Home Page
Xem About/Profile
Xem Projects
Xem Project Detail
Xem Technical Notes
Xem Technical Note Detail
Xem Technology Detail
Tải CV
Bấm GitHub
Bấm LinkedIn
Liên hệ qua email
```

Public Visitor không thể:

```txt id="wphx4d"
Đăng nhập
Tạo tài khoản
Sửa nội dung
Upload file
Xem draft
Xem unpublished content
Xem Admin Dashboard
Xem Audit Log
```

## 2.2 Admin

Admin là người quản lý CMS.

Admin cần đăng nhập để:

```txt id="2lkqih"
Quản lý Profile
Quản lý Social Links
Upload và active CV
Quản lý Projects
Quản lý Technical Notes
Quản lý Technologies
Quản lý Categories
Quản lý Tags
Quản lý Media Assets
Xem Audit Logs
Publish / Unpublish / Archive nội dung
```

## 2.3 Future Roles

MVP chỉ cần:

```txt id="2y71ev"
ADMIN
```

Nhưng backend chuẩn bị enum:

```txt id="odt88i"
ADMIN
EDITOR
REVIEWER
VIEWER
```

---

# 3. System Architecture

## 3.1 High-level Architecture

```txt id="aybs45"
ReactJS Frontend
  ├── Public Portfolio Website
  └── Admin Dashboard

Spring Boot Backend
  ├── Public REST APIs
  ├── Admin REST APIs
  ├── Auth APIs
  ├── File Upload APIs
  └── Audit Log Service

PostgreSQL Database
  ├── Content tables
  ├── Relationship tables
  ├── Auth tables
  ├── Media tables
  └── Audit tables
```

## 3.2 Frontend

Frontend dùng:

```txt id="d3dzx7"
ReactJS
Vite
React Router
TanStack Query
Axios hoặc Fetch wrapper
React Hook Form
Zod
Tailwind CSS hoặc CSS Modules
```

Frontend gồm hai khu vực:

```txt id="lsvpkq"
Public Website
Admin Dashboard
```

## 3.3 Backend

Backend dùng:

```txt id="0zgw73"
Java
Spring Boot
Spring Security
Spring Data JPA
Hibernate
JWT
REST API
Flyway
PostgreSQL
```

## 3.4 Database

Database dùng:

```txt id="t4g6a9"
PostgreSQL
```

Migration dùng:

```txt id="uzja2j"
Flyway
```

## 3.5 Deployment

MVP deploy bằng:

```txt id="tfj7c3"
Docker
Docker Compose
Backend container
Frontend container
PostgreSQL container
```

---

# 4. Global Product Rules

## 4.1 Public Access Rule

```txt id="lq7vlf"
Public Website không cần login.
Public Visitor chỉ có quyền đọc dữ liệu đã public.
```

## 4.2 Admin Access Rule

```txt id="r6vuea"
Admin Dashboard bắt buộc login.
Admin APIs bắt buộc có access token hợp lệ.
```

## 4.3 Public Content Rule

Public API chỉ trả dữ liệu:

```txt id="av1zab"
Profile ACTIVE
Profile Content ACTIVE
Project PUBLISHED
Technical Note PUBLISHED
Technology ACTIVE
Category ACTIVE
Tag ACTIVE
CV File ACTIVE
Media Asset READY + PUBLIC
```

Không trả:

```txt id="b8wsuz"
DRAFT
SCHEDULED
UNPUBLISHED
ARCHIVED
DELETED
FAILED
PRIVATE
Password hash
Refresh token hash
Audit log
Admin metadata
```

## 4.4 Soft Delete Rule

Các entity quan trọng dùng soft delete:

```txt id="jbvpxx"
users
profiles
profile_contents
projects
technical_notes
technologies
categories
tags
cv_files
media_assets
```

## 4.5 Slug Rule

Public URL dùng slug, không dùng ID.

Ví dụ:

```txt id="tvfy63"
/projects/portfolio-cms-admin
/notes/how-jwt-authentication-works
/technologies/spring-boot
```

## 4.6 Audit Rule

Các action quan trọng phải ghi Audit Log:

```txt id="uzt41s"
Login success
Login failed
Logout
Create content
Update content
Publish content
Unpublish content
Archive content
Delete content
Upload CV
Activate CV
Upload Media
Attach Media
Change status
```

---

# 5. Core Entities

## 5.1 Content Entities

```txt id="fo3o6w"
Profile
Profile Content
Project
Technical Note
Technology
Category
Tag
Skill Group
Social Link
```

## 5.2 File / Media Entities

```txt id="0btx91"
CV File
Media Asset
Media Usage
```

## 5.3 Security Entities

```txt id="71si5h"
Admin Account / users
Refresh Token
Password Reset Token sau MVP
```

## 5.4 Audit Entities

```txt id="odvwsd"
Audit Log
Status History sau MVP
```

## 5.5 Relationship Entities

```txt id="j54f5n"
project_technologies
note_technologies
project_notes
note_tags
project_tags
skill_group_technologies
media_usages
```

---

# 6. Route Structure

## 6.1 Public Frontend Routes

```txt id="rjdz5j"
/
/about
/projects
/projects/:slug
/notes
/notes/:slug
/technologies/:slug
/cv
```

## 6.2 Admin Frontend Routes

```txt id="w4kktj"
/admin/login
/admin
/admin/profile
/admin/projects
/admin/projects/new
/admin/projects/:id/edit
/admin/notes
/admin/notes/new
/admin/notes/:id/edit
/admin/technologies
/admin/categories
/admin/tags
/admin/skill-groups
/admin/cv-files
/admin/media
/admin/audit-logs
```

## 6.3 Backend Public APIs

```txt id="gt8f3s"
GET /public/profile
GET /public/projects
GET /public/projects/{slug}
GET /public/notes
GET /public/notes/{slug}
GET /public/technologies/{slug}
GET /public/categories/{slug}/notes
GET /public/tags/{slug}/notes
GET /public/cv/download
```

## 6.4 Backend Admin APIs

```txt id="m6r1aq"
POST   /admin/projects
GET    /admin/projects
GET    /admin/projects/{id}
PUT    /admin/projects/{id}
PATCH  /admin/projects/{id}/status
DELETE /admin/projects/{id}

POST   /admin/notes
GET    /admin/notes
GET    /admin/notes/{id}
PUT    /admin/notes/{id}
PATCH  /admin/notes/{id}/status
DELETE /admin/notes/{id}

POST   /admin/media-assets
GET    /admin/media-assets
PUT    /admin/media-assets/{id}
DELETE /admin/media-assets/{id}

GET    /admin/audit-logs
```

## 6.5 Auth APIs

```txt id="45tez1"
POST /auth/login
POST /auth/refresh
POST /auth/logout
GET  /auth/me
```

---

# 7. Database Summary

## 7.1 Must-have Tables

```txt id="zq21uo"
users
refresh_tokens

profiles
profile_contents
social_links

projects
technical_notes
technologies
categories
tags
skill_groups

project_technologies
note_technologies
project_notes
note_tags
project_tags
skill_group_technologies

cv_files

media_assets
media_usages

audit_logs
slug_redirects
```

## 7.2 Phase-later Tables

```txt id="jygdn3"
password_reset_tokens
media_variants
cv_download_events

note_status_history
project_status_history
technology_status_history
cv_status_history
media_status_history
user_status_history
taxonomy_status_history
```

## 7.3 Database Rules

```txt id="stgm80"
Primary key dùng BIGINT.
Public URL dùng slug.
Status lưu bằng VARCHAR + CHECK constraint.
Soft delete dùng deleted_at và deleted_by.
Slug unique theo partial unique index.
Many-to-many dùng bảng trung gian explicit.
Audit Log dùng JSONB cho old_value và new_value.
```

---

# 8. Frontend Project Structure

## 8.1 Recommended Structure

```txt id="7438gw"
src/
├── app/
│   ├── router.tsx
│   ├── App.tsx
│   └── providers.tsx
│
├── layouts/
│   ├── PublicLayout.tsx
│   └── AdminLayout.tsx
│
├── pages/
│   ├── public/
│   └── admin/
│
├── features/
│   ├── auth/
│   ├── profile/
│   ├── projects/
│   ├── notes/
│   ├── technologies/
│   ├── taxonomy/
│   ├── cv/
│   ├── media/
│   └── audit/
│
├── components/
│   ├── common/
│   ├── public/
│   └── admin/
│
├── services/
├── hooks/
├── types/
└── utils/
```

## 8.2 Frontend State Rules

```txt id="7rqs37"
Server state dùng TanStack Query.
Form state dùng React Hook Form.
Validation dùng Zod.
Auth state dùng React Context hoặc Zustand.
API client tự động attach token cho Admin API.
Public API không attach token.
```

---

# 9. Backend Project Structure

## 9.1 Recommended Structure

```txt id="3vdcqj"
src/main/java/com/example/portfolio/
├── auth/
├── user/
├── profile/
├── project/
├── note/
├── technology/
├── taxonomy/
├── cv/
├── media/
├── audit/
├── common/
│   ├── config/
│   ├── exception/
│   ├── security/
│   ├── dto/
│   └── util/
└── PortfolioApplication.java
```

## 9.2 Backend Layer Rules

```txt id="h1ng0k"
Controller chỉ nhận request và trả response.
Service chứa business logic.
Repository truy cập database.
DTO dùng cho request/response.
Entity không trả trực tiếp ra API.
Mapper chuyển Entity sang DTO.
Exception handler xử lý lỗi thống nhất.
```

---

# 10. Phase Roadmap Overview

Dự án chia thành 9 phase:

```txt id="ue9ai6"
Phase 0 — Product, Architecture & Planning
Phase 1 — Full-stack Project Foundation
Phase 2 — Authentication & Admin Shell
Phase 3 — Profile, Social Links & CV
Phase 4 — Taxonomy, Technology & Skill Groups
Phase 5 — Projects & Technical Notes CMS
Phase 6 — Media Asset & Audit Log
Phase 7 — Public Portfolio Website
Phase 8 — Testing, Security, SEO & Deployment
```

---

# 11. Phase 0 — Product, Architecture & Planning

## 11.1 Goal

Chốt sản phẩm, architecture, entity model, database model, API boundary và frontend/backend roadmap trước khi code.

## 11.2 Deliverables

```txt id="oj7m9c"
Product Vision
Target Users
Feature Scope
Entity Model
Database Schema Direction
API Boundary
Frontend Route Plan
Backend Module Plan
Phase Roadmap
```

## 11.3 User Stories

### US-0.1 — Define Product Vision

As a project owner,
I want to define the product vision,
so that the system has a clear direction before implementation.

Acceptance Criteria:

```txt id="d4ww2f"
Product name is defined.
Main goal is defined.
Target users are defined.
Public website and admin dashboard are separated.
Learning goals are documented.
```

### US-0.2 — Define Full-stack Architecture

As a developer,
I want to define backend and frontend architecture,
so that implementation can follow a clear structure.

Acceptance Criteria:

```txt id="wojiiq"
Backend stack is defined.
Frontend stack is defined.
Database is selected.
Deployment direction is selected.
Public API and Admin API boundaries are defined.
```

### US-0.3 — Define Entity Model

As a backend learner,
I want to define all main entities,
so that database design is not random.

Acceptance Criteria:

```txt id="7oncpv"
Profile entity is defined.
Project entity is defined.
Technical Note entity is defined.
Technology entity is defined.
Category / Tag entity is defined.
CV File entity is defined.
Media Asset entity is defined.
Admin Account entity is defined.
Audit Log entity is defined.
Entity relationships are documented.
```

### US-0.4 — Define Frontend Route Map

As a frontend developer,
I want to define all public and admin routes,
so that React Router can be planned correctly.

Acceptance Criteria:

```txt id="vwnzsh"
Public routes are listed.
Admin routes are listed.
Login route is listed.
Route protection rule is defined.
Not found route is planned.
```

## 11.4 Phase 0 Definition of Done

```txt id="fw49dy"
Product spec exists.
Entity spec exists.
Database direction exists.
Frontend route plan exists.
Backend module plan exists.
Implementation phases are clear.
```

---

# 12. Phase 1 — Full-stack Project Foundation

## 12.1 Goal

Tạo nền móng backend, frontend, database, Docker và cấu trúc project.

## 12.2 Backend User Stories

### US-1.1 — Initialize Spring Boot Backend

As a developer,
I want to create a Spring Boot backend project,
so that the system has a backend foundation.

Acceptance Criteria:

```txt id="bdt4mc"
Spring Boot project is created.
Application starts locally.
Layered package structure exists.
Health check endpoint works.
Global exception handler exists.
CORS config is prepared for React frontend.
```

### US-1.2 — Setup PostgreSQL Connection

As a developer,
I want backend to connect to PostgreSQL,
so that data can be persisted.

Acceptance Criteria:

```txt id="c7ty7p"
PostgreSQL dependency is configured.
Database connection works locally.
Environment variables are used for DB credentials.
Application fails clearly if DB connection is invalid.
```

### US-1.3 — Setup Flyway Migration

As a developer,
I want schema migrations to be versioned,
so that database changes are repeatable.

Acceptance Criteria:

```txt id="92qzat"
Flyway is configured.
Initial migration folder exists.
Migration runs on startup.
Database schema can be recreated from migration files.
No manual schema changes are required.
```

## 12.3 Frontend User Stories

### US-1.4 — Initialize ReactJS Frontend

As a frontend developer,
I want to create a ReactJS frontend project,
so that I can build both Admin Dashboard and Public Website.

Acceptance Criteria:

```txt id="7f1rb0"
ReactJS project is created with Vite.
Application starts locally.
React Router is installed and configured.
Base PublicLayout exists.
Base AdminLayout exists.
NotFound page exists.
```

### US-1.5 — Setup Frontend API Client

As a frontend developer,
I want to create an API client,
so that frontend can call backend consistently.

Acceptance Criteria:

```txt id="u6udew"
apiClient.ts exists.
Base URL is configurable by environment variable.
Public API methods can be called without token.
Admin API client can attach token later.
Global error handling structure exists.
```

### US-1.6 — Setup TanStack Query

As a frontend developer,
I want to configure TanStack Query,
so that server data can be fetched and cached properly.

Acceptance Criteria:

```txt id="gq3tzm"
QueryClientProvider is configured.
Basic query example works.
Loading state is handled.
Error state is handled.
```

## 12.4 DevOps User Stories

### US-1.7 — Setup Docker Compose

As a developer,
I want Docker Compose for local development,
so that backend, frontend and database can run consistently.

Acceptance Criteria:

```txt id="qkoqo8"
docker-compose.yml exists.
PostgreSQL service runs.
Backend service can run.
Frontend service can run.
Environment variables are separated from code.
```

## 12.5 Phase 1 Definition of Done

```txt id="62ir01"
Backend starts locally.
Frontend starts locally.
PostgreSQL runs locally.
Flyway migration works.
React Router works.
API client exists.
Docker Compose can start main services.
```

---

# 13. Phase 2 — Authentication & Admin Shell

## 13.1 Goal

Xây authentication backend và Admin Dashboard shell frontend.

## 13.2 Backend User Stories

### US-2.1 — Create Admin Account Table

As a backend developer,
I want a users table for CMS/Admin users,
so that Admin can authenticate.

Acceptance Criteria:

```txt id="i5eqi5"
users table exists.
users table is documented as CMS/Admin users only.
Email is unique.
Password hash is stored.
Role is stored.
Status is stored.
Public visitors are not stored in users.
```

### US-2.2 — Seed First Admin Account

As a system owner,
I want the system to create the first Admin account,
so that I can log in after setup.

Acceptance Criteria:

```txt id="q48qfl"
Seed process creates one ADMIN account.
Password is hashed with BCrypt.
Seed does not create duplicate admin if run again.
Admin status is ACTIVE.
```

### US-2.3 — Admin Login API

As an admin,
I want to log in with email and password,
so that I can access Admin Dashboard.

Acceptance Criteria:

```txt id="rgewly"
POST /auth/login exists.
Correct credentials return access token and refresh token.
Wrong credentials return generic error.
Only ACTIVE admin can login.
Password hash is never returned.
Login success is audited.
Login failure is audited.
```

### US-2.4 — JWT Access Token

As a backend system,
I want to issue JWT access tokens,
so that Admin APIs can authenticate requests.

Acceptance Criteria:

```txt id="f0rbk8"
JWT contains user_id and role.
JWT has expiration.
Invalid token returns 401.
Expired token returns 401.
Disabled admin cannot access Admin APIs.
```

### US-2.5 — Refresh Token

As an admin,
I want to refresh my access token,
so that I do not need to log in repeatedly.

Acceptance Criteria:

```txt id="ja4h1r"
refresh_tokens table exists.
Refresh token is stored as hash.
Raw refresh token is never stored.
POST /auth/refresh works.
Expired refresh token is rejected.
Revoked refresh token is rejected.
```

### US-2.6 — Logout API

As an admin,
I want to logout,
so that my refresh token becomes invalid.

Acceptance Criteria:

```txt id="95hglj"
POST /auth/logout exists.
Logout revokes current refresh token.
Revoked refresh token cannot be reused.
Logout action is audited.
```

### US-2.7 — Protect Admin APIs

As a system owner,
I want all /admin APIs protected,
so that public visitors cannot modify content.

Acceptance Criteria:

```txt id="d9eced"
Requests to /admin/** without token return 401.
Invalid token returns 401.
Valid token with inactive account returns 401 or 403.
Public /public/** routes remain open.
```

## 13.3 Frontend User Stories

### US-2.8 — Admin Login Page

As an admin,
I want a React login page,
so that I can log into the CMS.

Acceptance Criteria:

```txt id="yyzmq3"
/admin/login route exists.
Login form has email and password.
Form validates required fields.
Submit calls POST /auth/login.
Successful login redirects to /admin.
Failed login shows generic error.
Password is not stored after submit.
```

### US-2.9 — Frontend Auth State

As a frontend app,
I want to store authentication state,
so that protected routes know whether Admin is logged in.

Acceptance Criteria:

```txt id="bzhzdd"
Auth context or store exists.
Access token can be stored.
Admin info can be stored.
Logout clears auth state.
Auth state survives refresh based on selected token strategy.
```

### US-2.10 — Protected Admin Routes

As a system owner,
I want admin frontend routes to be protected,
so that unauthenticated visitors cannot see Admin Dashboard.

Acceptance Criteria:

```txt id="bqrzp5"
/admin routes require auth state.
Unauthenticated visitor redirects to /admin/login.
Authenticated admin can access /admin.
Expired token triggers refresh flow if implemented.
If refresh fails, user is logged out.
```

### US-2.11 — Admin Layout Shell

As an admin,
I want a dashboard layout,
so that I can navigate CMS modules.

Acceptance Criteria:

```txt id="ldhc5h"
AdminLayout has sidebar.
Sidebar has Profile, Projects, Notes, Technologies, Categories, Tags, CV Files, Media, Audit Logs.
Header shows admin email/name.
Logout button exists.
Logout calls backend logout API.
```

## 13.4 Phase 2 Definition of Done

```txt id="bfhevn"
Admin can log in from React UI.
Backend issues access token and refresh token.
Admin APIs are protected.
Public APIs are open.
Admin Dashboard shell exists.
Logout works.
Login/logout events are audited.
```

---

# 14. Phase 3 — Profile, Social Links & CV

## 14.1 Goal

Xây module Profile, Social Links và CV để public website có thông tin cá nhân, CTA liên hệ và tải CV.

## 14.2 Backend User Stories

### US-3.1 — Profile API

As an admin,
I want to manage my Profile,
so that public visitors can understand who I am.

Acceptance Criteria:

```txt id="pg14t8"
profiles table exists.
Admin can create profile.
Admin can update profile.
Profile has display_name, email, location, primary_role, career_direction, main_tech_focus.
Profile has status.
Profile supports soft delete.
Profile update creates audit log.
Public API returns only ACTIVE profile.
```

### US-3.2 — Profile Content API

As an admin,
I want profile content by language,
so that website can support English and Vietnamese.

Acceptance Criteria:

```txt id="pzgu2u"
profile_contents table exists.
Admin can create EN content.
Admin can create VI content.
Each content has headline, subheadline, short_bio, long_bio, SEO fields.
Only one ACTIVE content per profile + language is allowed.
Draft content is not returned publicly.
Public API can return profile content by language.
```

### US-3.3 — Social Links API

As an admin,
I want to manage Social Links,
so that visitors can access GitHub, LinkedIn and email.

Acceptance Criteria:

```txt id="s12vm0"
social_links table exists.
Admin can create social link.
Admin can update social link.
Admin can soft delete social link.
Supported platforms include GITHUB, LINKEDIN, EMAIL, PORTFOLIO, OTHER.
Public API returns only ACTIVE links.
Invalid URL is rejected.
```

### US-3.4 — CV File API

As an admin,
I want to upload and activate CV files,
so that recruiters can download the correct CV.

Acceptance Criteria:

```txt id="p9k4k2"
cv_files table exists.
Admin can upload PDF CV.
Only PDF is accepted.
File size limit is enforced.
CV has language, target_role, version, status.
Only one ACTIVE CV per language + target_role is allowed.
Public CV download returns only ACTIVE CV.
Upload and activation actions are audited.
```

## 14.3 Frontend User Stories

### US-3.5 — Admin Profile Page

As an admin,
I want to edit Profile in the dashboard,
so that public information is easy to update.

Acceptance Criteria:

```txt id="bw654x"
/admin/profile route exists.
Profile form loads existing profile.
Admin can edit basic fields.
Admin can save changes.
Validation errors are shown.
Success message is shown.
```

### US-3.6 — Admin Profile Content Form

As an admin,
I want to edit EN and VI profile content,
so that public website can be bilingual.

Acceptance Criteria:

```txt id="p8vcki"
EN content form exists.
VI content form exists.
Headline is required.
SEO title and description fields exist.
Admin can save draft.
Admin can set content active if backend supports status change.
```

### US-3.7 — Admin Social Links UI

As an admin,
I want to manage social links from UI,
so that I do not need to call API manually.

Acceptance Criteria:

```txt id="x6bov3"
Admin can view social links.
Admin can add social link.
Admin can edit social link.
Admin can delete social link.
Display order can be set.
```

### US-3.8 — Admin CV Files UI

As an admin,
I want to upload and activate CV files,
so that recruiters get the right CV.

Acceptance Criteria:

```txt id="xn7m4c"
/admin/cv-files route exists.
Admin can view CV list.
Admin can upload PDF.
Admin can set language and target role.
Admin can activate CV.
Active CV is visually highlighted.
Invalid file type shows error.
```

### US-3.9 — Public Profile Data Rendering

As a public visitor,
I want to see profile and CV info without login,
so that I can understand and contact the portfolio owner.

Acceptance Criteria:

```txt id="c50w7p"
Public frontend can fetch /public/profile.
No login is required.
Profile headline is displayed.
Social links are displayed.
Download CV CTA is displayed.
Draft profile content is not shown.
```

## 14.4 Phase 3 Definition of Done

```txt id="7rpfr6"
Profile backend works.
Profile frontend works.
Social links work.
CV upload works.
CV activation rule works.
Public profile data can be rendered.
Audit log records major profile/CV actions.
```

---

# 15. Phase 4 — Taxonomy, Technology & Skill Groups

## 15.1 Goal

Xây master data cho Category, Tag, Technology và Skill Group.

## 15.2 Backend User Stories

### US-4.1 — Category Management API

As an admin,
I want to manage Categories,
so that Technical Notes can be grouped by main topic.

Acceptance Criteria:

```txt id="wc9e4e"
categories table exists.
Admin can create category.
Admin can update category.
Admin can archive category.
Admin can soft delete category.
Slug is unique among non-deleted categories.
Only ACTIVE categories appear publicly.
Category actions are audited.
```

### US-4.2 — Tag Management API

As an admin,
I want to manage Tags,
so that Projects and Notes can have flexible labels.

Acceptance Criteria:

```txt id="qjrqav"
tags table exists.
Admin can create tag.
Admin can update tag.
Admin can archive tag.
Admin can soft delete tag.
Slug is unique among non-deleted tags.
Only ACTIVE tags appear publicly.
Duplicate slug is rejected.
```

### US-4.3 — Technology Management API

As an admin,
I want to manage Technologies,
so that Projects and Notes can show technical stack.

Acceptance Criteria:

```txt id="fcx9s2"
technologies table exists.
Admin can create technology.
Admin can update technology.
Technology has name, slug, type, status, description, how_i_use_it.
Technology type must be valid.
Slug is unique among non-deleted technologies.
Only ACTIVE technologies appear publicly.
Technology can be marked is_core.
```

### US-4.4 — Skill Group API

As an admin,
I want to group technologies,
so that public skills section is organized.

Acceptance Criteria:

```txt id="1pwtp1"
skill_groups table exists.
skill_group_technologies table exists.
Admin can create skill group.
Admin can attach technology to skill group.
Admin can set display_order.
Public API returns ACTIVE skill groups with ACTIVE technologies.
```

## 15.3 Frontend User Stories

### US-4.5 — Category Admin UI

As an admin,
I want to manage categories in dashboard,
so that I can organize notes.

Acceptance Criteria:

```txt id="2e03sq"
/admin/categories route exists.
Category list displays name, slug, status.
Admin can create category.
Admin can edit category.
Admin can archive category.
Duplicate slug error is displayed.
```

### US-4.6 — Tag Admin UI

As an admin,
I want to manage tags in dashboard,
so that I can label content consistently.

Acceptance Criteria:

```txt id="qbmqvz"
/admin/tags route exists.
Tag list displays name, slug, status and usage_count if available.
Admin can create tag.
Admin can edit tag.
Admin can archive tag.
Duplicate slug error is displayed.
```

### US-4.7 — Technology Admin UI

As an admin,
I want to manage technologies,
so that projects and notes can use structured technology data.

Acceptance Criteria:

```txt id="u1m9rv"
/admin/technologies route exists.
Technology list displays name, type, status, is_core.
Admin can create technology.
Admin can edit technology.
Admin can set status.
Admin can mark as core.
Admin can set display_order.
```

### US-4.8 — Skill Group Admin UI

As an admin,
I want to manage skill groups,
so that public skills section is clear.

Acceptance Criteria:

```txt id="qexdgd"
/admin/skill-groups route exists.
Admin can create skill group.
Admin can attach technologies.
Admin can order technologies.
Skill group preview displays technologies.
```

## 15.4 Phase 4 Definition of Done

```txt id="nwm338"
Categories work backend and frontend.
Tags work backend and frontend.
Technologies work backend and frontend.
Skill groups work backend and frontend.
Public API can return active technology/skill data.
Slug uniqueness works.
```

---

# 16. Phase 5 — Projects & Technical Notes CMS

## 16.1 Goal

Xây hai content module quan trọng nhất: Projects và Technical Notes.

## 16.2 Backend User Stories — Projects

### US-5.1 — Create Project API

As an admin,
I want to create a Project as draft,
so that I can prepare project case studies before publishing.

Acceptance Criteria:

```txt id="9250wv"
projects table exists.
POST /admin/projects exists.
Project can be created as DRAFT.
Project has title, slug, language, summary.
Project has content_status and project_status.
Slug is unique per language among non-deleted projects.
Create action is audited.
```

### US-5.2 — Update Project API

As an admin,
I want to update Project details,
so that project case studies are accurate.

Acceptance Criteria:

```txt id="8ebx5s"
PUT /admin/projects/{id} exists.
Admin can update problem, solution, backend_highlights, architecture_notes, database_notes, security_notes, challenges, lessons_learned.
Update is transactional.
Update action is audited.
Invalid project id returns 404.
```

### US-5.3 — Project Relationships API

As an admin,
I want to attach technologies, tags and notes to a Project,
so that project content has rich evidence.

Acceptance Criteria:

```txt id="2yifxi"
Admin can attach technologies to project.
Admin can attach tags to project.
Admin can link related notes.
Duplicate relation is rejected.
Deleted technology/tag/note cannot be attached.
Public project returns only ACTIVE technologies, ACTIVE tags and PUBLISHED notes.
```

### US-5.4 — Publish Project API

As an admin,
I want to publish a Project,
so that public visitors can view it.

Acceptance Criteria:

```txt id="34xnc9"
PATCH /admin/projects/{id}/status supports PUBLISHED.
Publish validates required fields.
Required fields include title, slug, summary, role, project_status, SEO fields and at least one technology.
published_at is set.
Project appears in Public API.
Publish action is audited.
```

### US-5.5 — Public Project APIs

As a public visitor,
I want to browse and view projects,
so that I can evaluate practical experience.

Acceptance Criteria:

```txt id="la2r1u"
GET /public/projects returns only PUBLISHED projects.
GET /public/projects/{slug} returns only PUBLISHED project.
Draft/unpublished/archived/deleted projects return 404 or are hidden.
Response includes technologies, tags and related notes if public.
```

## 16.3 Backend User Stories — Technical Notes

### US-5.6 — Create Technical Note API

As an admin,
I want to create Technical Notes as draft,
so that I can document backend learning.

Acceptance Criteria:

```txt id="rtk5mn"
technical_notes table exists.
POST /admin/notes exists.
Note can be created as DRAFT.
Note has title, slug, language, excerpt, content.
Slug is unique per language among non-deleted notes.
Create action is audited.
```

### US-5.7 — Update Technical Note API

As an admin,
I want to update Technical Note content,
so that articles remain accurate.

Acceptance Criteria:

```txt id="86c02t"
PUT /admin/notes/{id} exists.
Admin can update title, slug, excerpt, content, SEO fields.
Admin can assign category.
Admin can attach tags and technologies.
Admin can link related projects.
Update action is audited.
```

### US-5.8 — Publish Technical Note API

As an admin,
I want to publish Technical Notes,
so that visitors can read them.

Acceptance Criteria:

```txt id="4cso8m"
PATCH /admin/notes/{id}/status supports PUBLISHED.
Publish validates title, slug, excerpt, content, category, language, SEO title and SEO description.
published_at is set.
Published note appears in Public API.
Draft note does not appear in Public API.
Publish action is audited.
```

### US-5.9 — Public Technical Note APIs

As a public visitor,
I want to browse and read notes,
so that I can understand the owner's technical thinking.

Acceptance Criteria:

```txt id="bgxz5t"
GET /public/notes returns only PUBLISHED notes.
GET /public/notes/{slug} returns only PUBLISHED note.
Draft/scheduled/unpublished/deleted notes are hidden.
Response includes category, tags, technologies and related projects if public.
```

## 16.4 Frontend User Stories — Projects

### US-5.10 — Admin Project List UI

As an admin,
I want to see all projects,
so that I can manage project content.

Acceptance Criteria:

```txt id="3crhmv"
/admin/projects route exists.
Project list shows title, language, content_status, project_status, updated_at.
Admin can search by title.
Admin can filter by status.
Admin can open edit page.
Admin can create new project.
```

### US-5.11 — Admin Project Form UI

As an admin,
I want to create and edit project case studies,
so that I can publish strong backend evidence.

Acceptance Criteria:

```txt id="f5wk1e"
Project form supports title, slug, summary, description.
Project form supports problem, solution, role, project_type.
Project form supports backend_highlights, architecture_notes, database_notes, security_notes, challenges, lessons_learned.
Admin can select technologies.
Admin can select tags.
Admin can link related notes.
Admin can save draft.
Admin can publish if validation passes.
Validation errors are shown clearly.
```

## 16.5 Frontend User Stories — Technical Notes

### US-5.12 — Admin Technical Note List UI

As an admin,
I want to see all notes,
so that I can manage technical content.

Acceptance Criteria:

```txt id="2o8ofa"
/admin/notes route exists.
Note list shows title, language, status, category, updated_at.
Admin can search by title.
Admin can filter by status.
Admin can filter by category.
Admin can create new note.
Admin can open edit page.
```

### US-5.13 — Admin Technical Note Editor UI

As an admin,
I want to write and edit notes,
so that I can document technical learning.

Acceptance Criteria:

```txt id="3nt6tw"
Note form supports title, slug, excerpt, content.
Markdown editor or textarea exists.
Preview mode exists.
Admin can select category.
Admin can select tags.
Admin can select technologies.
Admin can link related projects.
Admin can save draft.
Admin can publish if validation passes.
```

## 16.6 Phase 5 Definition of Done

```txt id="ysrg3l"
Project backend works.
Project admin UI works.
Technical Note backend works.
Technical Note admin UI works.
Relationships work.
Publish workflow works.
Public APIs expose only published content.
Audit log records important actions.
```

---

# 17. Phase 6 — Media Asset & Audit Log

## 17.1 Goal

Xây Media Library và Audit Log để CMS giống hệ thống thực tế hơn.

## 17.2 Backend User Stories

### US-6.1 — Media Upload API

As an admin,
I want to upload media assets,
so that I can use images, diagrams and screenshots in content.

Acceptance Criteria:

```txt id="1ewfdm"
media_assets table exists.
POST /admin/media-assets supports multipart upload.
Allowed file types are validated.
File size limit is enforced.
Media metadata is stored.
Successful upload sets status READY.
Failed upload does not create READY asset.
Upload action is audited.
```

### US-6.2 — Media Usage API

As an admin,
I want to attach media to content,
so that projects and notes can include visual evidence.

Acceptance Criteria:

```txt id="xsb4am"
media_usages table exists.
Admin can attach READY media to supported entity.
Supported entity types include PROJECT, TECHNICAL_NOTE, TECHNOLOGY, PROFILE.
Supported usage types include COVER_IMAGE, THUMBNAIL, SCREENSHOT, DIAGRAM, CONTENT_IMAGE, OG_IMAGE, ICON, AVATAR.
Deleted or failed media cannot be attached.
```

### US-6.3 — Media Delete Protection

As a system owner,
I want to prevent deleting media used by public content,
so that public pages do not break.

Acceptance Criteria:

```txt id="yva7v3"
System checks media_usages before delete.
If media is used by public content, hard delete is blocked.
Admin receives usage information.
Soft delete is allowed only by defined rule.
Public pages use fallback if media is missing.
```

### US-6.4 — Audit Log Service

As a system owner,
I want important actions to be logged,
so that I can trace system changes.

Acceptance Criteria:

```txt id="u4xbnw"
audit_logs table exists.
Audit log stores actor, action, entity_type, entity_id, result, created_at.
Audit log can store old_value and new_value as JSONB.
Audit log does not store passwords, tokens or secrets.
Audit log is created for major admin actions.
```

### US-6.5 — Audit Log Admin API

As an admin,
I want to view audit logs,
so that I can inspect CMS activity.

Acceptance Criteria:

```txt id="wkxv8q"
GET /admin/audit-logs exists.
Admin can filter by action.
Admin can filter by entity_type.
Admin can filter by actor.
Admin can filter by date range.
Audit logs are read-only.
Public API does not expose audit logs.
```

## 17.3 Frontend User Stories

### US-6.6 — Media Library UI

As an admin,
I want a Media Library page,
so that I can manage uploaded files.

Acceptance Criteria:

```txt id="s2lc09"
/admin/media route exists.
Admin can upload media.
Admin can view media list or grid.
Admin can edit title, alt_text and caption.
Admin can see status and visibility.
Admin can see where media is used if backend provides usage data.
Admin is warned before deleting used media.
```

### US-6.7 — Attach Media in Content Forms

As an admin,
I want to attach media in Project and Note forms,
so that content can include screenshots and diagrams.

Acceptance Criteria:

```txt id="4ij7ug"
Project form can select cover image.
Project form can attach screenshots or diagrams.
Note editor can attach content images or diagrams.
Only READY media can be selected.
Selected media preview is shown.
```

### US-6.8 — Audit Log UI

As an admin,
I want to view audit logs,
so that I can understand what changed in the CMS.

Acceptance Criteria:

```txt id="v97ha5"
/admin/audit-logs route exists.
Audit log list shows time, actor, action, entity type, entity title and result.
Admin can filter logs.
Admin can open log detail.
Old/new values are displayed in readable format.
Audit log page is read-only.
```

## 17.4 Phase 6 Definition of Done

```txt id="n84on6"
Media upload works.
Media usage tracking works.
Media delete protection works.
Audit log service works.
Audit log admin API works.
Media Library UI works.
Audit Log UI works.
Sensitive data is not logged.
```

---

# 18. Phase 7 — Public Portfolio Website

## 18.1 Goal

Xây Public Portfolio Website bằng ReactJS để nhà tuyển dụng xem tự do không cần đăng nhập.

## 18.2 Backend User Stories

### US-7.1 — Public Profile API

As a public visitor,
I want to fetch public profile data,
so that I can understand the portfolio owner.

Acceptance Criteria:

```txt id="b3ntw2"
GET /public/profile exists.
No login is required.
Only ACTIVE profile and ACTIVE profile content are returned.
Social links are returned if ACTIVE.
Active CV information is returned.
Admin metadata is not returned.
```

### US-7.2 — Public Projects API

As a public visitor,
I want to fetch published projects,
so that I can evaluate practical experience.

Acceptance Criteria:

```txt id="fqormc"
GET /public/projects exists.
GET /public/projects/{slug} exists.
Only PUBLISHED projects are returned.
Deleted projects are hidden.
Draft/unpublished/archived projects are hidden.
Related technologies/tags/notes are filtered by public status.
```

### US-7.3 — Public Notes API

As a public visitor,
I want to fetch published notes,
so that I can read technical content.

Acceptance Criteria:

```txt id="jt6s44"
GET /public/notes exists.
GET /public/notes/{slug} exists.
Only PUBLISHED notes are returned.
Draft/scheduled/unpublished/deleted notes are hidden.
Related projects are filtered by PUBLISHED status.
```

### US-7.4 — Public Technology API

As a public visitor,
I want to view technology detail,
so that I can see related projects and notes.

Acceptance Criteria:

```txt id="85zm7n"
GET /public/technologies/{slug} exists.
Only ACTIVE technology is returned.
Related projects are PUBLISHED.
Related notes are PUBLISHED.
Inactive/deleted technology returns 404.
```

### US-7.5 — Public CV Download API

As a recruiter,
I want to download the active CV,
so that I can review candidate information.

Acceptance Criteria:

```txt id="jhzhv1"
GET /public/cv/download exists.
No login is required.
Only ACTIVE CV can be downloaded.
Language and target_role selection are supported.
Inactive/deleted CV cannot be downloaded.
```

## 18.3 Frontend User Stories

### US-7.6 — Public Home Page

As a public visitor,
I want to view Home Page,
so that I can quickly understand the portfolio owner.

Acceptance Criteria:

```txt id="knbek4"
/ route exists.
Home page does not require login.
Home page shows headline and subheadline.
Home page shows core technologies.
Home page shows featured projects.
Home page shows latest notes.
Home page shows Download CV CTA.
Home page shows GitHub and LinkedIn links.
```

### US-7.7 — Public Projects Page

As a recruiter,
I want to browse projects,
so that I can evaluate practical backend experience.

Acceptance Criteria:

```txt id="ey612e"
/projects route exists.
Page fetches /public/projects.
Only published projects are displayed.
Project card shows title, summary, technologies and tags.
Project card links to /projects/:slug.
Loading state exists.
Empty state exists.
```

### US-7.8 — Public Project Detail Page

As a technical interviewer,
I want to view project details,
so that I can evaluate implementation thinking.

Acceptance Criteria:

```txt id="2nx7dy"
/projects/:slug route exists.
Page fetches /public/projects/{slug}.
Page shows problem, solution, backend highlights, architecture notes, database notes, security notes, challenges and lessons learned.
Page shows technologies and tags.
Page shows related technical notes.
Page shows GitHub/demo/API docs links if available.
404 state is shown if project is not public.
```

### US-7.9 — Public Notes Page

As a technical reader,
I want to browse technical notes,
so that I can understand technical thinking.

Acceptance Criteria:

```txt id="0hpdud"
/notes route exists.
Page fetches /public/notes.
Only published notes are shown.
Note card shows title, excerpt, category, tags, technologies and reading time.
Note card links to /notes/:slug.
Loading and empty states exist.
```

### US-7.10 — Public Note Detail Page

As a technical reader,
I want to read a full technical note,
so that I can learn the topic in detail.

Acceptance Criteria:

```txt id="47mc77"
/notes/:slug route exists.
Page fetches /public/notes/{slug}.
Page renders markdown content.
Code blocks are readable.
Category, tags and technologies are shown.
Related projects are shown.
404 state is shown if note is not public.
```

### US-7.11 — Public Technology Detail Page

As a visitor,
I want to view a technology page,
so that I can see related projects and notes.

Acceptance Criteria:

```txt id="d4bxkb"
/technologies/:slug route exists.
Page fetches /public/technologies/{slug}.
Technology name, type, description and how_i_use_it are shown.
Related published projects are shown.
Related published notes are shown.
404 state is shown if technology is inactive or missing.
```

### US-7.12 — Public CV Download

As a recruiter,
I want to download CV,
so that I can review candidate profile.

Acceptance Criteria:

```txt id="lnhgoo"
Download CV button is visible.
No login is required.
Clicking button downloads or opens active CV.
If CV unavailable, friendly message is shown.
```

### US-7.13 — Public Contact Section

As a recruiter,
I want to contact the owner,
so that I can discuss opportunities.

Acceptance Criteria:

```txt id="117ztw"
Contact section shows public email.
GitHub link works.
LinkedIn link works.
CV CTA is visible.
No login is required.
```

## 18.4 Phase 7 Definition of Done

```txt id="rx10sz"
Public APIs work.
Public React pages work.
Public website requires no login.
Draft/admin data is not exposed.
Projects, notes, technologies, CV and contact are visible.
404/loading/empty states exist.
```

---

# 19. Phase 8 — Testing, Security, SEO & Deployment

## 19.1 Goal

Hoàn thiện hệ thống để đủ tin cậy, an toàn và có thể deploy/demo.

## 19.2 Backend User Stories

### US-8.1 — Backend Integration Tests

As a developer,
I want backend integration tests,
so that critical workflows are protected.

Acceptance Criteria:

```txt id="3ty2lg"
Auth login test exists.
Refresh token test exists.
Public visibility test exists.
Project publish workflow test exists.
Technical Note publish workflow test exists.
CV active rule test exists.
Media delete protection test exists.
Audit log test exists.
```

### US-8.2 — Security Review

As a system owner,
I want to review backend security,
so that admin data remains protected.

Acceptance Criteria:

```txt id="ah9izp"
Admin APIs require token.
Public APIs do not expose draft/unpublished/deleted content.
Password hash is never returned.
Refresh token raw is not stored.
File upload validates type and size.
Audit log does not store sensitive secrets.
CORS is configured safely.
```

### US-8.3 — Backend Dockerization

As a developer,
I want backend Dockerfile,
so that backend can run consistently in deployment.

Acceptance Criteria:

```txt id="d0ok07"
Backend Dockerfile exists.
Backend container starts successfully.
Environment variables are configurable.
Application connects to PostgreSQL container.
Flyway migrations run in container environment.
```

## 19.3 Frontend User Stories

### US-8.4 — Frontend Error Handling Polish

As a user,
I want clear error states,
so that I understand what happened when something fails.

Acceptance Criteria:

```txt id="0gj6x7"
Public 404 page exists.
Admin unauthorized state exists.
Loading states are consistent.
Empty states are consistent.
Server errors do not expose internal details.
Form validation messages are clear.
```

### US-8.5 — Frontend SEO Basics

As a public visitor or search engine,
I want public pages to have metadata,
so that shared links and search results look professional.

Acceptance Criteria:

```txt id="4esn8a"
React Helmet or equivalent is configured.
Home page has title and meta description.
Project detail page has dynamic title and meta description.
Note detail page has dynamic title and meta description.
Technology page has dynamic title and meta description.
OG image field is supported where available.
Admin pages are not intended for indexing.
```

### US-8.6 — Frontend Responsive Design

As a visitor,
I want the site to work on desktop and mobile,
so that I can view portfolio from any device.

Acceptance Criteria:

```txt id="a4kb2k"
Public Home is responsive.
Projects page is responsive.
Note detail page is readable on mobile.
Admin dashboard is usable on desktop.
Basic mobile behavior exists for admin, even if desktop-first.
```

### US-8.7 — Frontend Dockerization

As a developer,
I want frontend Dockerfile,
so that React app can be deployed consistently.

Acceptance Criteria:

```txt id="1gw7ez"
Frontend Dockerfile exists.
Frontend build succeeds.
Frontend can be served in container.
API base URL is configurable by environment variable.
```

## 19.4 DevOps User Stories

### US-8.8 — Full Docker Compose

As a developer,
I want full Docker Compose setup,
so that the whole system can run with one command.

Acceptance Criteria:

```txt id="3qn30f"
docker-compose includes PostgreSQL.
docker-compose includes backend.
docker-compose includes frontend.
Backend can reach database.
Frontend can reach backend.
System can run from clean environment.
```

### US-8.9 — Production Environment Config

As a system owner,
I want environment config separated,
so that secrets are not hard-coded.

Acceptance Criteria:

```txt id="l30xv7"
Database credentials are environment variables.
JWT secret is environment variable.
File storage path is environment variable.
Frontend API URL is environment variable.
No secrets are committed to repository.
```

## 19.5 Phase 8 Definition of Done

```txt id="p2vvfy"
Backend tests cover critical workflows.
Security review is completed.
Frontend has clear loading/error/empty states.
SEO metadata exists for public pages.
Responsive design basics are done.
Docker setup works.
System can be deployed/demoed.
```

---

# 20. Cross-phase Acceptance Criteria

## 20.1 Public/Admin Separation

```txt id="q53v20"
Public visitors never need login.
Admin Dashboard always requires login.
Public API never returns draft/admin-only data.
Admin API never allows unauthenticated writes.
```

## 20.2 Data Integrity

```txt id="xmr2xw"
Slug uniqueness is enforced.
Foreign keys are enforced.
Duplicate relationship rows are prevented.
One active CV per language + target_role is enforced.
One active profile content per profile + language is enforced.
Deleted master data cannot be newly attached.
```

## 20.3 Status Workflow

```txt id="ps6ltd"
Project public visibility depends on content_status = PUBLISHED.
Note public visibility depends on status = PUBLISHED.
Technology public visibility depends on status = ACTIVE.
Media public visibility depends on status = READY and visibility = PUBLIC.
CV public download depends on status = ACTIVE.
```

## 20.4 Security

```txt id="uffijl"
Passwords are hashed with BCrypt.
Refresh tokens are stored as hash.
Access tokens expire.
Admin status is checked.
Disabled admin cannot access Admin APIs.
Public visitor cannot upload files.
Public visitor cannot modify data.
```

## 20.5 Frontend Quality

```txt id="bsstsa"
React routes are organized.
Admin routes are protected.
API client handles errors.
Forms validate input.
Loading states exist.
Empty states exist.
404 pages exist.
Frontend does not expose secrets.
```

## 20.6 Audit Coverage

```txt id="dp1e13"
Login success is audited.
Login failure is audited.
Logout is audited.
Create content is audited.
Update content is audited.
Publish/unpublish/archive/delete actions are audited.
CV upload/activation is audited.
Media upload/delete is audited.
```

---

# 21. MVP Release Criteria

MVP được coi là hoàn thành khi:

```txt id="9th9ts"
Admin can login from React Admin Dashboard.
Admin can manage Profile and Social Links.
Admin can upload and activate CV.
Admin can manage Technologies, Categories, Tags and Skill Groups.
Admin can create, edit and publish Projects.
Admin can create, edit and publish Technical Notes.
Admin can upload and attach Media Assets.
System records Audit Logs.
Public React Website shows Profile, Projects, Notes, Technologies and CV.
Public visitor does not need login.
Draft/unpublished/deleted content is not public.
System can run with Docker Compose.
```

---

# 22. Suggested Implementation Order

## 22.1 Backend First

```txt id="qy70zn"
1. Spring Boot setup
2. PostgreSQL + Flyway
3. users + refresh_tokens
4. Auth APIs
5. Security config
6. Audit Log base service
7. Profile APIs
8. CV APIs
9. Taxonomy APIs
10. Technology APIs
11. Project APIs
12. Technical Note APIs
13. Media APIs
14. Public APIs
15. Tests
```

## 22.2 Frontend Parallel Path

```txt id="ac08wb"
1. ReactJS + Vite setup
2. Router setup
3. API client setup
4. Auth context
5. Login page
6. Protected AdminLayout
7. Admin Profile page
8. Admin taxonomy pages
9. Admin Project pages
10. Admin Note pages
11. Media Library
12. Audit Log page
13. Public Home
14. Public Projects
15. Public Notes
16. Public Technology pages
17. Public CV/contact
18. Polish
```

## 22.3 Recommended Practical Order

```txt id="qktfeb"
Build backend auth first.
Then build frontend login.
Then build one full vertical slice: Profile backend + Admin UI + Public display.
Then repeat vertical slices for CV, Technology, Project, Note, Media.
```

Vertical slice approach giúp học tốt hơn vì mỗi module đi từ database → backend → API → frontend admin → frontend public.

---

# 23. Learning Outcomes

Sau khi hoàn thành hệ thống này, người triển khai sẽ học được:

```txt id="2yq2yy"
Full-stack product planning
Backend architecture with Spring Boot
REST API design
PostgreSQL schema design
Flyway migration
Authentication with JWT
Refresh token flow
Admin route protection
ReactJS frontend architecture
React Router
TanStack Query
React Hook Form
Admin Dashboard CRUD
Public Website rendering
Markdown content rendering
File upload
Audit logging
Public/Admin data separation
Docker deployment
Testing critical workflows
```

---

# 24. Final Summary

Hệ thống Portfolio CMS được xây theo mô hình:

```txt id="uhw7kt"
ReactJS Frontend
+
Java Spring Boot Backend
+
PostgreSQL Database
```

Frontend gồm:

```txt id="2cutqr"
Public Portfolio Website
Admin Dashboard
```

Backend gồm:

```txt id="gqevxw"
Auth APIs
Admin APIs
Public APIs
Media APIs
Audit Log Service
```

Nguyên tắc quan trọng nhất:

```txt id="kp73wj"
Nhà tuyển dụng xem portfolio tự do, không cần đăng nhập.
Admin phải đăng nhập để quản lý nội dung.
Public API chỉ trả dữ liệu đã publish/active.
Admin API được bảo vệ bằng authentication và authorization.
```

Đây là một dự án full-stack đủ mạnh để chứng minh năng lực:

```txt id="qki6gm"
Java Spring Boot Backend
PostgreSQL Database Design
JWT Authentication
ReactJS Admin Dashboard
ReactJS Public Portfolio Website
CMS Workflow
Media Library
Audit Log
SEO-friendly Portfolio Content
```
