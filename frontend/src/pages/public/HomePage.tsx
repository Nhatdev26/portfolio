import { useQuery } from "@tanstack/react-query";

import { getBackendHealth } from "../../services/health";

export function HomePage() {
  const health = useQuery({
    queryKey: ["backend-health"],
    queryFn: getBackendHealth
  });

  return (
    <section className="hero-section">
      <div className="hero-copy">
        <p className="eyebrow">Backend Developer Portfolio</p>
        <h1>Portfolio CMS</h1>
        <p className="hero-text">
          A public portfolio and private CMS foundation for projects, technical
          notes, technologies, CVs, media, and audit history.
        </p>
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

