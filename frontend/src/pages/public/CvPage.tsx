import { useQuery } from "@tanstack/react-query";

import { activeCvExists, publicCvDownloadUrl } from "../../services/cv";

export function CvPage() {
  const cvQuery = useQuery({
    queryKey: ["public-cv", "EN", "backend-developer"],
    queryFn: () => activeCvExists("EN", "backend-developer"),
    retry: false
  });
  const downloadUrl = publicCvDownloadUrl("EN", "backend-developer");

  return (
    <section className="public-detail">
      <p className="eyebrow">CV</p>
      <h1>Download CV</h1>
      <p className="lead-text">
        Get the latest active CV for backend engineering opportunities.
      </p>
      {cvQuery.isLoading && <p className="muted">Checking active CV...</p>}
      {cvQuery.data ? (
        <div className="hero-links compact-links">
          <a href={downloadUrl}>Download PDF</a>
        </div>
      ) : null}
      {(cvQuery.isError || cvQuery.data === false) && (
        <p className="form-error" role="status">
          Active CV is not available yet.
        </p>
      )}
    </section>
  );
}
