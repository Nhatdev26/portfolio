import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { getBackendHealth } from "../../services/health";
import { getPublicProfile } from "../../services/profile";
import { publicCvDownloadUrl } from "../../services/cv";
import { listPublicNotes, listPublicProjects } from "../../services/content";

const focusAreas = [
  {
    title: "Backend systems",
    text: "Spring Boot APIs, authentication, audit trails, and data integrity that can hold real CMS workflows."
  },
  {
    title: "Frontend craft",
    text: "Responsive React interfaces with polished states, strong information hierarchy, and useful admin flows."
  },
  {
    title: "Delivery mindset",
    text: "Docker-first validation, story-sized changes, and documentation that keeps the product easy to evolve."
  }
];

export function HomePage() {
  const health = useQuery({
    queryKey: ["backend-health"],
    queryFn: getBackendHealth
  });
  const profile = useQuery({
    queryKey: ["public-profile", "home", "EN"],
    queryFn: () => getPublicProfile("EN"),
    retry: false
  });
  const projects = useQuery({
    queryKey: ["public-projects", "home-preview"],
    queryFn: listPublicProjects
  });
  const notes = useQuery({
    queryKey: ["public-notes", "home-preview"],
    queryFn: listPublicNotes
  });

  const heroTitle = profile.data?.headline ?? "Portfolio CMS";
  const heroText =
    profile.data?.subheadline ??
    profile.data?.shortBio ??
    "A public portfolio and private CMS foundation for projects, technical notes, technologies, CVs, media, and audit history.";

  return (
    <>
      <section className="hero-section portfolio-hero">
        <div className="hero-copy reveal-in">
          <p className="eyebrow">{profile.data?.primaryRole ?? "Backend Developer Portfolio"}</p>
          <h1>{heroTitle}</h1>
          <p className="hero-text">{heroText}</p>
          <div className="hero-links">
            <Link to="/projects">View my work</Link>
            <a className="secondary" href={publicCvDownloadUrl("EN", "backend-developer")}>Download CV</a>
          </div>
          {profile.data?.socialLinks.length ? (
            <div className="social-strip" aria-label="Social links">
              {profile.data.socialLinks.map((link) => (
                <a key={`${link.platform}-${link.url}`} href={link.url}>
                  {link.label}
                </a>
              ))}
            </div>
          ) : null}
        </div>

        <div className="hero-visual reveal-in" aria-label="Portfolio status">
          <div className="portrait-badge" aria-hidden="true">
            <span>{initials(profile.data?.displayName ?? heroTitle)}</span>
          </div>
          <div className="status-panel hero-status" aria-live="polite">
            <span className="status-label">Backend</span>
            {health.isLoading && <strong>Checking</strong>}
            {health.isError && <strong>Offline</strong>}
            {health.data && <strong>{health.data.status}</strong>}
            <small>{health.data?.service ?? "Spring Boot API"}</small>
          </div>
          <div className="hero-signal-card">
            <span>Current stack</span>
            <strong>React · Spring Boot · PostgreSQL</strong>
          </div>
        </div>
      </section>

      <section className="home-section">
        <div className="section-heading centered">
          <p className="eyebrow">What I build</p>
          <h2>Practical systems with a polished face</h2>
        </div>
        <div className="focus-grid">
          {focusAreas.map((area) => (
            <article className="feature-card" key={area.title}>
              <span className="feature-index" aria-hidden="true" />
              <h3>{area.title}</h3>
              <p>{area.text}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="home-section split-showcase">
        <div>
          <p className="eyebrow">Selected work</p>
          <h2>Projects</h2>
          <p className="muted">Published case studies from the CMS appear here automatically.</p>
          <Link className="button-link" to="/projects">Explore projects</Link>
        </div>
        <div className="mini-list">
          {(projects.data ?? []).slice(0, 3).map((project) => (
            <Link className="mini-card" key={project.slug} to={`/projects/${project.slug}`}>
              <span>{project.projectType}</span>
              <strong>{project.title}</strong>
              <small>{project.summary}</small>
            </Link>
          ))}
          {!projects.isLoading && !projects.data?.length && (
            <div className="mini-card empty-card">
              <strong>Projects are coming online</strong>
              <small>Publish a project in the CMS to feature it here.</small>
            </div>
          )}
        </div>
      </section>

      <section className="home-section split-showcase reverse">
        <div>
          <p className="eyebrow">Technical writing</p>
          <h2>Notes</h2>
          <p className="muted">Short technical notes become a lightweight blog for implementation details.</p>
          <Link className="button-link secondary" to="/notes">Read notes</Link>
        </div>
        <div className="mini-list">
          {(notes.data ?? []).slice(0, 3).map((note) => (
            <Link className="mini-card" key={note.slug} to={`/notes/${note.slug}`}>
              <span>{note.category?.name ?? "Note"}</span>
              <strong>{note.title}</strong>
              <small>{note.excerpt}</small>
            </Link>
          ))}
          {!notes.isLoading && !notes.data?.length && (
            <div className="mini-card empty-card">
              <strong>Notes are waiting</strong>
              <small>Publish a note in the CMS to fill this section.</small>
            </div>
          )}
        </div>
      </section>

      <section className="contact-band">
        <p className="eyebrow">Contact</p>
        <h2>Ready to turn the CMS into a sharper portfolio?</h2>
        <div className="hero-links compact-links">
          <Link to="/about">About me</Link>
          <a className="secondary" href={publicCvDownloadUrl("EN", "backend-developer")}>Get CV</a>
        </div>
      </section>
    </>
  );
}

function initials(value: string) {
  return value
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("") || "PC";
}
