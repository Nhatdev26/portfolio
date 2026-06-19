UPDATE projects
SET content_status = 'ARCHIVED',
    updated_at = now()
WHERE deleted_at IS NULL
  AND (
      slug LIKE 'us010-smoke-%'
      OR slug LIKE 'portfolio-cms-api-%'
  );

UPDATE technical_notes
SET status = 'ARCHIVED',
    updated_at = now()
WHERE deleted_at IS NULL
  AND (
      slug LIKE 'us010-smoke-%'
      OR slug LIKE 'jwt-session-notes-%'
  );

UPDATE technologies
SET status = 'ARCHIVED',
    updated_at = now()
WHERE deleted_at IS NULL
  AND slug LIKE 'spring-boot-%';

WITH active_profile AS (
    INSERT INTO profiles (
        display_name,
        email,
        location,
        primary_role,
        career_direction,
        main_tech_focus,
        status,
        created_at,
        updated_at
    )
    VALUES (
        'Nhat Nguyen',
        'contact@nhatdev.dev',
        'Vietnam',
        'Backend Developer',
        'Building reliable Java/Spring Boot systems and full-stack portfolio products',
        'Java, Spring Boot, PostgreSQL, React, Docker, JWT, CI/CD',
        'ACTIVE',
        now(),
        now()
    )
    ON CONFLICT (status) WHERE status = 'ACTIVE' AND deleted_at IS NULL
    DO UPDATE SET
        display_name = EXCLUDED.display_name,
        email = EXCLUDED.email,
        location = EXCLUDED.location,
        primary_role = EXCLUDED.primary_role,
        career_direction = EXCLUDED.career_direction,
        main_tech_focus = EXCLUDED.main_tech_focus,
        updated_at = now()
    RETURNING id
)
INSERT INTO profile_contents (
    profile_id,
    language,
    headline,
    subheadline,
    short_bio,
    long_bio,
    seo_title,
    seo_description,
    status,
    created_at,
    updated_at
)
SELECT
    active_profile.id,
    'EN',
    'Backend developer building production-minded portfolio systems',
    'I design secure Java/Spring Boot APIs, PostgreSQL data models, React admin workflows, and public portfolio experiences that can be tested, shipped, and explained.',
    'I focus on backend-heavy full-stack products: authentication, authorization, content workflows, media handling, audit logs, Docker validation, and clear API contracts.',
    'This portfolio is built as a real CMS rather than a static page. The backend owns authentication, JWT refresh sessions, public/admin API separation, content publishing rules, file/media storage, audit logging, and PostgreSQL migrations. The frontend provides a protected admin dashboard plus a public site for introduction, projects, skills, and technical writing. I use the project to demonstrate practical engineering habits: story-sized delivery, Docker-first verification, CI checks, browser smoke testing, and documentation that keeps future changes understandable.',
    'Nhat Nguyen - Backend Developer Portfolio',
    'Backend developer portfolio showcasing Java, Spring Boot, PostgreSQL, React, Docker, JWT auth, audit logs, media workflows, and full-stack CMS delivery.',
    'ACTIVE',
    now(),
    now()
FROM active_profile
ON CONFLICT (profile_id, language) WHERE status = 'ACTIVE' AND deleted_at IS NULL
DO UPDATE SET
    headline = EXCLUDED.headline,
    subheadline = EXCLUDED.subheadline,
    short_bio = EXCLUDED.short_bio,
    long_bio = EXCLUDED.long_bio,
    seo_title = EXCLUDED.seo_title,
    seo_description = EXCLUDED.seo_description,
    updated_at = now();

WITH active_profile AS (
    SELECT id FROM profiles WHERE status = 'ACTIVE' AND deleted_at IS NULL ORDER BY id LIMIT 1
)
INSERT INTO social_links (
    profile_id,
    platform,
    label,
    url,
    display_order,
    status,
    created_at,
    updated_at
)
SELECT active_profile.id, link.platform, link.label, link.url, link.display_order, 'ACTIVE', now(), now()
FROM active_profile
CROSS JOIN (
    VALUES
        ('GITHUB', 'GitHub', 'https://github.com/Nhatdev26', 1),
        ('EMAIL', 'Email', 'mailto:contact@nhatdev.dev', 2)
) AS link(platform, label, url, display_order)
WHERE NOT EXISTS (
    SELECT 1
    FROM social_links existing
    WHERE existing.profile_id = active_profile.id
      AND existing.platform = link.platform
      AND existing.deleted_at IS NULL
);

INSERT INTO categories (name, slug, description, status, display_order, created_at, updated_at)
VALUES
    ('Backend Engineering', 'backend-engineering', 'Notes about API design, persistence, auth, and service boundaries.', 'ACTIVE', 1, now(), now()),
    ('Full-stack Delivery', 'full-stack-delivery', 'Notes about shipping a complete product from backend to frontend and deployment.', 'ACTIVE', 2, now(), now()),
    ('Testing And Deployment', 'testing-and-deployment', 'Notes about validation, Docker smoke tests, CI, and release readiness.', 'ACTIVE', 3, now(), now())
ON CONFLICT (slug) WHERE deleted_at IS NULL
DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    display_order = EXCLUDED.display_order,
    updated_at = now();

INSERT INTO tags (name, slug, status, display_order, created_at, updated_at)
VALUES
    ('CMS', 'cms', 'ACTIVE', 1, now(), now()),
    ('Authentication', 'authentication', 'ACTIVE', 2, now(), now()),
    ('API Design', 'api-design', 'ACTIVE', 3, now(), now()),
    ('Database', 'database', 'ACTIVE', 4, now(), now()),
    ('Testing', 'testing', 'ACTIVE', 5, now(), now()),
    ('Deployment', 'deployment', 'ACTIVE', 6, now(), now()),
    ('Frontend', 'frontend', 'ACTIVE', 7, now(), now())
ON CONFLICT (slug) WHERE deleted_at IS NULL
DO UPDATE SET
    name = EXCLUDED.name,
    status = EXCLUDED.status,
    display_order = EXCLUDED.display_order,
    updated_at = now();

INSERT INTO technologies (
    name,
    slug,
    type,
    status,
    description,
    how_i_use_it,
    is_core,
    display_order,
    created_at,
    updated_at
)
VALUES
    ('Java', 'java', 'LANGUAGE', 'ACTIVE', 'Primary backend language for service code, domain rules, and application tests.', 'I use Java to model backend workflows with explicit types, readable service boundaries, and maintainable tests.', true, 1, now(), now()),
    ('Spring Boot', 'spring-boot', 'FRAMEWORK', 'ACTIVE', 'Backend framework for REST APIs, security, validation, persistence, and application configuration.', 'I use Spring Boot for the portfolio API, admin/public route separation, JWT authentication, and service-layer workflows.', true, 2, now(), now()),
    ('PostgreSQL', 'postgresql', 'DATABASE', 'ACTIVE', 'Relational database for content, auth, audit, media metadata, and publishing rules.', 'I use PostgreSQL with Flyway migrations, constraints, partial indexes, JSONB audit data, and many-to-many content relationships.', true, 3, now(), now()),
    ('React', 'react', 'FRAMEWORK', 'ACTIVE', 'Frontend library for the public portfolio and the protected admin dashboard.', 'I use React with routed public pages, reusable admin forms, TanStack Query server state, and responsive layouts.', true, 4, now(), now()),
    ('TypeScript', 'typescript', 'LANGUAGE', 'ACTIVE', 'Typed frontend language for API contracts, page data models, and safer UI code.', 'I use TypeScript to keep API responses, admin payloads, and public page rendering aligned with backend contracts.', true, 5, now(), now()),
    ('Docker', 'docker', 'DEVOPS', 'ACTIVE', 'Container runtime for repeatable local and release validation.', 'I use Docker Compose to run PostgreSQL, backend, and frontend together, then smoke test the complete system before deployment.', true, 6, now(), now()),
    ('Flyway', 'flyway', 'DATABASE', 'ACTIVE', 'Database migration tool for repeatable schema and seed evolution.', 'I use Flyway to version schema changes and seed public portfolio content in a way that fresh environments can reproduce.', true, 7, now(), now()),
    ('JWT', 'jwt', 'OTHER', 'ACTIVE', 'Token format used for admin access sessions and refresh-token based authentication.', 'I use JWT access tokens with refresh-token rotation, protected admin APIs, and frontend session refresh handling.', true, 8, now(), now()),
    ('GitHub Actions', 'github-actions', 'DEVOPS', 'ACTIVE', 'CI service for validating backend, frontend, env template, and Docker smoke checks.', 'I use GitHub Actions to run the same checks expected before merge and deployment.', false, 9, now(), now()),
    ('Testcontainers', 'testcontainers', 'TESTING', 'ACTIVE', 'Integration testing support for Spring Boot against real PostgreSQL containers.', 'I use Testcontainers to prove critical auth, content visibility, media, CV, and audit workflows with realistic infrastructure.', false, 10, now(), now())
ON CONFLICT (slug) WHERE deleted_at IS NULL
DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    how_i_use_it = EXCLUDED.how_i_use_it,
    is_core = EXCLUDED.is_core,
    display_order = EXCLUDED.display_order,
    updated_at = now();

INSERT INTO skill_groups (name, slug, description, status, display_order, created_at, updated_at)
VALUES
    ('Backend Core', 'backend-core', 'Languages and frameworks used to build secure, maintainable APIs.', 'ACTIVE', 1, now(), now()),
    ('Data And Persistence', 'data-and-persistence', 'Relational modeling, migrations, constraints, and content visibility rules.', 'ACTIVE', 2, now(), now()),
    ('Frontend And Delivery', 'frontend-and-delivery', 'Public portfolio UI, admin dashboard workflows, and release validation tooling.', 'ACTIVE', 3, now(), now())
ON CONFLICT (slug) WHERE deleted_at IS NULL
DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    display_order = EXCLUDED.display_order,
    updated_at = now();

INSERT INTO skill_group_technologies (skill_group_id, technology_id, display_order)
SELECT sg.id, t.id, mapping.display_order
FROM (
    VALUES
        ('backend-core', 'java', 1),
        ('backend-core', 'spring-boot', 2),
        ('backend-core', 'jwt', 3),
        ('data-and-persistence', 'postgresql', 1),
        ('data-and-persistence', 'flyway', 2),
        ('data-and-persistence', 'testcontainers', 3),
        ('frontend-and-delivery', 'react', 1),
        ('frontend-and-delivery', 'typescript', 2),
        ('frontend-and-delivery', 'docker', 3),
        ('frontend-and-delivery', 'github-actions', 4)
) AS mapping(skill_group_slug, technology_slug, display_order)
JOIN skill_groups sg ON sg.slug = mapping.skill_group_slug AND sg.deleted_at IS NULL
JOIN technologies t ON t.slug = mapping.technology_slug AND t.deleted_at IS NULL
ON CONFLICT (skill_group_id, technology_id)
DO UPDATE SET display_order = EXCLUDED.display_order;

INSERT INTO technical_notes (
    title,
    slug,
    language,
    excerpt,
    content,
    category_id,
    status,
    seo_title,
    seo_description,
    reading_minutes,
    published_at,
    display_order,
    created_at,
    updated_at
)
SELECT
    note.title,
    note.slug,
    'EN',
    note.excerpt,
    note.content,
    category.id,
    'PUBLISHED',
    note.seo_title,
    note.seo_description,
    note.reading_minutes,
    now() - (note.display_order || ' days')::interval,
    note.display_order,
    now(),
    now()
FROM (
    VALUES
        (
            'How this portfolio separates public and admin APIs',
            'public-admin-api-separation',
            'A practical note on keeping recruiters on public read APIs while CMS writes stay behind authenticated admin routes.',
            '# How this portfolio separates public and admin APIs

The public website and the admin dashboard use different API surfaces.

Public routes expose only active or published content:

- active profile content
- published projects
- published technical notes
- active technologies
- public ready media

Admin routes require a valid bearer token and are responsible for content management, media uploads, audit visibility, and publishing decisions.

This split keeps the portfolio easy to browse while preserving a clean security boundary for CMS operations.',
            'Backend API separation in a Spring Boot portfolio CMS',
            'How the portfolio CMS separates public read APIs from protected admin APIs for safer content workflows.',
            5,
            1,
            'backend-engineering'
        ),
        (
            'Designing a publish workflow for portfolio projects',
            'designing-project-publish-workflow',
            'A short explanation of draft versus published content, slug-based URLs, and relationship filtering for public project pages.',
            '# Designing a publish workflow for portfolio projects

A portfolio CMS should not expose work-in-progress content by accident.

Projects in this system carry a content status. Public APIs return only `PUBLISHED` projects and keep draft or archived records out of the public site.

Each project also has a slug, technology links, tags, SEO metadata, and optional related notes. This lets a project detail page explain the problem, solution, backend decisions, frontend work, and lessons learned without hard-coding content into React.',
            'Project publishing workflow for a portfolio CMS',
            'Draft, published, and archived project states in a Spring Boot and React portfolio CMS.',
            6,
            2,
            'full-stack-delivery'
        ),
        (
            'What I test before calling a portfolio CMS ready',
            'portfolio-cms-validation-checklist',
            'The validation ladder I use before merge or deploy: backend tests, frontend typecheck/build, env checks, Docker smoke, and browser QA.',
            '# What I test before calling a portfolio CMS ready

For this project, a change is not done just because the UI renders once.

The usual validation ladder is:

- backend unit and integration tests
- frontend typecheck and production build
- production environment template validation
- Docker Compose smoke test
- browser checks across public and admin routes
- GitHub Actions CI before merge

That proof matters because the portfolio is also a demonstration of engineering habits, not only a gallery of screens.',
            'Portfolio CMS validation checklist',
            'A practical validation checklist for a Java Spring Boot, PostgreSQL, Docker, and React portfolio CMS.',
            4,
            3,
            'testing-and-deployment'
        )
) AS note(title, slug, excerpt, content, seo_title, seo_description, reading_minutes, display_order, category_slug)
JOIN categories category ON category.slug = note.category_slug AND category.deleted_at IS NULL
ON CONFLICT (slug, language) WHERE deleted_at IS NULL
DO UPDATE SET
    title = EXCLUDED.title,
    excerpt = EXCLUDED.excerpt,
    content = EXCLUDED.content,
    category_id = EXCLUDED.category_id,
    status = EXCLUDED.status,
    seo_title = EXCLUDED.seo_title,
    seo_description = EXCLUDED.seo_description,
    reading_minutes = EXCLUDED.reading_minutes,
    published_at = EXCLUDED.published_at,
    display_order = EXCLUDED.display_order,
    updated_at = now();

INSERT INTO projects (
    title,
    slug,
    language,
    summary,
    description,
    role,
    project_type,
    project_status,
    content_status,
    problem_statement,
    solution_overview,
    backend_highlights,
    frontend_highlights,
    architecture_notes,
    source_url,
    demo_url,
    seo_title,
    seo_description,
    published_at,
    display_order,
    created_at,
    updated_at
)
VALUES
    (
        'Portfolio CMS System',
        'portfolio-cms-system',
        'EN',
        'A full-stack portfolio CMS with protected admin workflows, public portfolio pages, JWT auth, media handling, audit logs, Docker validation, and CI.',
        'This project turns a portfolio into a real content product. The admin dashboard manages profile content, technologies, projects, notes, media, CV files, and audit logs. The public site renders only active or published records for visitors.',
        'Full-stack developer',
        'FULL_STACK',
        'MAINTAINED',
        'PUBLISHED',
        'A static portfolio cannot demonstrate backend architecture, authentication, relational modeling, content workflow, media safety, and deployment habits in one place.',
        'Build a CMS-backed portfolio with Spring Boot, PostgreSQL, React, Docker Compose, and GitHub Actions so every public page is powered by managed content.',
        'Spring Security JWT auth, refresh tokens, protected admin APIs, Flyway migrations, PostgreSQL constraints, audit logs, CV/media workflows, and integration tests with Testcontainers.',
        'React public site, protected admin dashboard, TanStack Query data loading, responsive pages, SEO metadata, and Docker-served production build.',
        'The system separates public read APIs from admin write APIs. Data moves through explicit DTOs, service-layer rules, database constraints, and browser-tested public/admin routes.',
        'https://github.com/Nhatdev26/portfolio',
        null,
        'Portfolio CMS System - Java Spring Boot and React',
        'A full-stack Portfolio CMS using Java, Spring Boot, PostgreSQL, React, Docker, JWT auth, media management, audit logs, and CI validation.',
        now(),
        1,
        now(),
        now()
    ),
    (
        'Repository Harness Workflow',
        'repository-harness-workflow',
        'EN',
        'A project operating layer that turns broad requests into story-sized work, durable decisions, validation proof, and repeatable agent collaboration.',
        'The repository uses Harness docs, story packets, trace records, and a CLI-backed proof matrix to keep implementation grounded in product intent and validation evidence.',
        'Backend and workflow implementer',
        'INFRASTRUCTURE',
        'MAINTAINED',
        'PUBLISHED',
        'AI-assisted development can drift when the repo does not remember why decisions were made or what proof a change requires.',
        'Use Harness intake, stories, decisions, test matrix, and trace records so every feature has a clear lane, scope, evidence, and next-step memory.',
        'Durable SQLite-backed Harness records, story verification commands, decision logs, trace scoring, and tool registry checks.',
        'The frontend benefits indirectly: UI changes are planned as scoped stories and verified with browser smoke checks rather than ad hoc edits.',
        'Harness sits beside the app code as an operational layer. It does not replace tests; it makes tests and product contracts easier to find and trust.',
        'https://github.com/Nhatdev26/portfolio',
        null,
        'Repository Harness Workflow for Portfolio CMS',
        'How this portfolio repo uses Harness stories, decisions, validation proof, and traces to keep full-stack work reliable.',
        now() - interval '1 day',
        2,
        now(),
        now()
    ),
    (
        'Secure Admin Content Workflow',
        'secure-admin-content-workflow',
        'EN',
        'A CMS workflow for creating, publishing, and auditing portfolio content without exposing drafts or admin metadata to public visitors.',
        'The admin content workflow covers profile updates, taxonomy management, project and note publishing, media attachment, CV activation, and audit log visibility.',
        'Backend-focused full-stack developer',
        'BACKEND',
        'COMPLETED',
        'PUBLISHED',
        'A portfolio CMS needs safe write workflows, but public visitors should never see draft records, inactive technologies, private media, or admin metadata.',
        'Protect admin APIs with JWT auth, require explicit published/active statuses for public APIs, and keep audit records for important CMS actions.',
        'Admin API protection, content status rules, relationship filtering, media delete protection, active CV enforcement, and audit redaction.',
        'Admin pages expose focused CRUD workflows with validation, table/list views, media pickers, and protected route handling.',
        'Public pages consume read-only endpoints; admin pages attach access tokens and handle refresh sessions through the frontend auth boundary.',
        'https://github.com/Nhatdev26/portfolio',
        null,
        'Secure Admin Content Workflow',
        'A secure CMS workflow for admin-only writes, public-only reads, status-based publishing, and audit logging.',
        now() - interval '2 days',
        3,
        now(),
        now()
    )
ON CONFLICT (slug, language) WHERE deleted_at IS NULL
DO UPDATE SET
    title = EXCLUDED.title,
    summary = EXCLUDED.summary,
    description = EXCLUDED.description,
    role = EXCLUDED.role,
    project_type = EXCLUDED.project_type,
    project_status = EXCLUDED.project_status,
    content_status = EXCLUDED.content_status,
    problem_statement = EXCLUDED.problem_statement,
    solution_overview = EXCLUDED.solution_overview,
    backend_highlights = EXCLUDED.backend_highlights,
    frontend_highlights = EXCLUDED.frontend_highlights,
    architecture_notes = EXCLUDED.architecture_notes,
    source_url = EXCLUDED.source_url,
    demo_url = EXCLUDED.demo_url,
    seo_title = EXCLUDED.seo_title,
    seo_description = EXCLUDED.seo_description,
    published_at = EXCLUDED.published_at,
    display_order = EXCLUDED.display_order,
    updated_at = now();

INSERT INTO project_technologies (project_id, technology_id)
SELECT project.id, technology.id
FROM (
    VALUES
        ('portfolio-cms-system', 'java'),
        ('portfolio-cms-system', 'spring-boot'),
        ('portfolio-cms-system', 'postgresql'),
        ('portfolio-cms-system', 'react'),
        ('portfolio-cms-system', 'typescript'),
        ('portfolio-cms-system', 'docker'),
        ('portfolio-cms-system', 'jwt'),
        ('repository-harness-workflow', 'java'),
        ('repository-harness-workflow', 'postgresql'),
        ('repository-harness-workflow', 'docker'),
        ('repository-harness-workflow', 'github-actions'),
        ('secure-admin-content-workflow', 'spring-boot'),
        ('secure-admin-content-workflow', 'postgresql'),
        ('secure-admin-content-workflow', 'jwt'),
        ('secure-admin-content-workflow', 'react')
) AS mapping(project_slug, technology_slug)
JOIN projects project ON project.slug = mapping.project_slug AND project.language = 'EN' AND project.deleted_at IS NULL
JOIN technologies technology ON technology.slug = mapping.technology_slug AND technology.deleted_at IS NULL
ON CONFLICT (project_id, technology_id) DO NOTHING;

INSERT INTO project_tags (project_id, tag_id)
SELECT project.id, tag.id
FROM (
    VALUES
        ('portfolio-cms-system', 'cms'),
        ('portfolio-cms-system', 'api-design'),
        ('portfolio-cms-system', 'testing'),
        ('portfolio-cms-system', 'deployment'),
        ('repository-harness-workflow', 'testing'),
        ('repository-harness-workflow', 'deployment'),
        ('secure-admin-content-workflow', 'authentication'),
        ('secure-admin-content-workflow', 'api-design'),
        ('secure-admin-content-workflow', 'database')
) AS mapping(project_slug, tag_slug)
JOIN projects project ON project.slug = mapping.project_slug AND project.language = 'EN' AND project.deleted_at IS NULL
JOIN tags tag ON tag.slug = mapping.tag_slug AND tag.deleted_at IS NULL
ON CONFLICT (project_id, tag_id) DO NOTHING;

INSERT INTO note_technologies (note_id, technology_id)
SELECT note.id, technology.id
FROM (
    VALUES
        ('public-admin-api-separation', 'spring-boot'),
        ('public-admin-api-separation', 'jwt'),
        ('public-admin-api-separation', 'postgresql'),
        ('designing-project-publish-workflow', 'spring-boot'),
        ('designing-project-publish-workflow', 'postgresql'),
        ('designing-project-publish-workflow', 'react'),
        ('portfolio-cms-validation-checklist', 'docker'),
        ('portfolio-cms-validation-checklist', 'github-actions'),
        ('portfolio-cms-validation-checklist', 'testcontainers')
) AS mapping(note_slug, technology_slug)
JOIN technical_notes note ON note.slug = mapping.note_slug AND note.language = 'EN' AND note.deleted_at IS NULL
JOIN technologies technology ON technology.slug = mapping.technology_slug AND technology.deleted_at IS NULL
ON CONFLICT (note_id, technology_id) DO NOTHING;

INSERT INTO note_tags (note_id, tag_id)
SELECT note.id, tag.id
FROM (
    VALUES
        ('public-admin-api-separation', 'api-design'),
        ('public-admin-api-separation', 'authentication'),
        ('designing-project-publish-workflow', 'cms'),
        ('designing-project-publish-workflow', 'database'),
        ('portfolio-cms-validation-checklist', 'testing'),
        ('portfolio-cms-validation-checklist', 'deployment')
) AS mapping(note_slug, tag_slug)
JOIN technical_notes note ON note.slug = mapping.note_slug AND note.language = 'EN' AND note.deleted_at IS NULL
JOIN tags tag ON tag.slug = mapping.tag_slug AND tag.deleted_at IS NULL
ON CONFLICT (note_id, tag_id) DO NOTHING;

INSERT INTO project_notes (project_id, note_id)
SELECT project.id, note.id
FROM (
    VALUES
        ('portfolio-cms-system', 'public-admin-api-separation'),
        ('portfolio-cms-system', 'designing-project-publish-workflow'),
        ('portfolio-cms-system', 'portfolio-cms-validation-checklist'),
        ('secure-admin-content-workflow', 'public-admin-api-separation'),
        ('secure-admin-content-workflow', 'designing-project-publish-workflow'),
        ('repository-harness-workflow', 'portfolio-cms-validation-checklist')
) AS mapping(project_slug, note_slug)
JOIN projects project ON project.slug = mapping.project_slug AND project.language = 'EN' AND project.deleted_at IS NULL
JOIN technical_notes note ON note.slug = mapping.note_slug AND note.language = 'EN' AND note.deleted_at IS NULL
ON CONFLICT (project_id, note_id) DO NOTHING;
