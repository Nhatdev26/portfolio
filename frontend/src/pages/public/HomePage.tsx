import { useQuery } from "@tanstack/react-query";

import { getBackendHealth } from "../../services/health";
import { getPublicProfile } from "../../services/profile";

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

  const heroTitle = profile.data?.headline ?? "Portfolio CMS";
  const heroText =
    profile.data?.subheadline ??
    profile.data?.shortBio ??
    "A public portfolio and private CMS foundation for projects, technical notes, technologies, CVs, media, and audit history.";

  return (
    <section className="hero-section">
      <div className="hero-copy">
        <p className="eyebrow">{profile.data?.primaryRole ?? "Backend Developer Portfolio"}</p>
        <h1>{heroTitle}</h1>
        <p className="hero-text">{heroText}</p>
        {profile.data?.socialLinks.length ? (
          <div className="hero-links">
            {profile.data.socialLinks.map((link) => (
              <a key={`${link.platform}-${link.url}`} href={link.url}>
                {link.label}
              </a>
            ))}
          </div>
        ) : null}
      </div>

      <div className="status-panel" aria-live="polite">
        <span className="status-label">Backend</span>
        {health.isLoading && <strong>Checking</strong>}
        {health.isError && <strong>Unavailable</strong>}
        {health.data && <strong>{health.data.status}</strong>}
        <small>{health.data?.service ?? "Spring Boot API"}</small>
      </div>
    </section>
  );
}
