import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";

import { getPublicTechnology } from "../../services/taxonomy";

export function TechnologyDetailPage() {
  const { slug } = useParams();
  const technologyQuery = useQuery({
    enabled: Boolean(slug),
    queryKey: ["public-technology", slug],
    queryFn: () => getPublicTechnology(slug as string)
  });
  const technology = technologyQuery.data;

  return (
    <section className="public-detail">
      {technologyQuery.isLoading && <p className="muted">Loading technology...</p>}
      {technologyQuery.isError && <p className="form-error">Technology could not be loaded.</p>}
      {technology && (
        <>
          <p className="eyebrow">{technology.type}{technology.core ? " · Core" : ""}</p>
          <h1>{technology.name}</h1>
          {technology.description && <p className="lead-text">{technology.description}</p>}
          {technology.howIUseIt && (
            <div className="article-body">
              <section>
                <h2>How I use it</h2>
                <p>{technology.howIUseIt}</p>
              </section>
            </div>
          )}
        </>
      )}
    </section>
  );
}
