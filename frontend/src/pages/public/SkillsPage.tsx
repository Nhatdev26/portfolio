import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { Seo } from "../../components/common/Seo";
import { getPublicTechnologies, type Technology } from "../../services/taxonomy";

const typeLabels: Record<Technology["type"], string> = {
  LANGUAGE: "Languages",
  FRAMEWORK: "Frameworks",
  DATABASE: "Databases",
  DEVOPS: "DevOps",
  CLOUD: "Cloud",
  TESTING: "Testing",
  TOOL: "Tools",
  OTHER: "Other"
};

export function SkillsPage() {
  const technologies = useQuery({
    queryKey: ["public-technologies", "skills"],
    queryFn: getPublicTechnologies
  });
  const grouped = useMemo(() => groupByType(technologies.data ?? []), [technologies.data]);
  const coreSkills = (technologies.data ?? []).filter((technology) => technology.core).slice(0, 6);

  return (
    <section className="public-index skills-page">
      <Seo
        title="Skills"
        description="Explore the active technologies, frameworks, databases, and delivery tools behind this backend developer portfolio."
        canonicalPath="/skills"
      />
      <div className="section-heading">
        <p className="eyebrow">Skills</p>
        <h1>Tools I use to ship reliable web systems</h1>
        <p className="lead-text">
          A focused view of my active technology stack, from backend fundamentals to delivery tooling.
        </p>
      </div>

      {coreSkills.length ? (
        <div className="core-skill-strip" aria-label="Core skills">
          {coreSkills.map((technology) => (
            <Link key={technology.slug} to={`/technologies/${technology.slug}`}>
              {technology.name}
            </Link>
          ))}
        </div>
      ) : null}

      {technologies.isLoading && <p className="muted">Loading skills...</p>}
      {technologies.isError && <p className="form-error">Skills could not be loaded.</p>}

      <div className="skill-group-grid">
        {grouped.map(([type, items]) => (
          <article className="skill-group-card" key={type}>
            <div>
              <p className="eyebrow">{typeLabels[type]}</p>
              <h2>{items.length} skill{items.length === 1 ? "" : "s"}</h2>
            </div>
            <div className="skill-pill-grid">
              {items.map((technology) => (
                <Link key={technology.slug} to={`/technologies/${technology.slug}`}>
                  <strong>{technology.name}</strong>
                  {technology.core && <span>Core</span>}
                </Link>
              ))}
            </div>
          </article>
        ))}
      </div>

      {!technologies.isLoading && !technologies.data?.length && (
        <div className="media-empty public-empty">
          <strong>No public skills yet</strong>
          <span>Publish active technologies in the CMS to populate this page.</span>
        </div>
      )}
    </section>
  );
}

function groupByType(technologies: Technology[]) {
  const active = technologies.filter((technology) => technology.status === "ACTIVE");
  const map = new Map<Technology["type"], Technology[]>();

  active.forEach((technology) => {
    const existing = map.get(technology.type) ?? [];
    map.set(technology.type, [...existing, technology]);
  });

  return Array.from(map.entries()).sort(([left], [right]) => left.localeCompare(right));
}
