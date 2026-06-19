import { useMemo, type CSSProperties } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { getPublicTechnologies, type Technology } from "../../services/taxonomy";

type SkillCategory = "Frontend Development" | "Backend Development" | "Data & Delivery";

const categoryOrder: SkillCategory[] = [
  "Frontend Development",
  "Backend Development",
  "Data & Delivery"
];

const skillMeta: Record<string, { category: SkillCategory; level: number; icon: string; accent: string }> = {
  react: { category: "Frontend Development", level: 90, icon: "R", accent: "#22d3ee" },
  typescript: { category: "Frontend Development", level: 88, icon: "TS", accent: "#3b82f6" },
  java: { category: "Backend Development", level: 88, icon: "J", accent: "#f97316" },
  "spring-boot": { category: "Backend Development", level: 86, icon: "SB", accent: "#22c55e" },
  jwt: { category: "Backend Development", level: 80, icon: "JWT", accent: "#fb7185" },
  postgresql: { category: "Data & Delivery", level: 84, icon: "DB", accent: "#60a5fa" },
  flyway: { category: "Data & Delivery", level: 78, icon: "FW", accent: "#ef4444" },
  docker: { category: "Data & Delivery", level: 86, icon: "D", accent: "#38bdf8" },
  "github-actions": { category: "Data & Delivery", level: 76, icon: "GA", accent: "#a78bfa" },
  testcontainers: { category: "Data & Delivery", level: 74, icon: "TC", accent: "#f59e0b" }
};

const fallbackMeta: Record<Technology["type"], { category: SkillCategory; level: number; icon: string; accent: string }> = {
  LANGUAGE: { category: "Backend Development", level: 78, icon: "L", accent: "#38bdf8" },
  FRAMEWORK: { category: "Frontend Development", level: 78, icon: "F", accent: "#22d3ee" },
  DATABASE: { category: "Data & Delivery", level: 78, icon: "DB", accent: "#60a5fa" },
  DEVOPS: { category: "Data & Delivery", level: 76, icon: "DV", accent: "#818cf8" },
  CLOUD: { category: "Data & Delivery", level: 72, icon: "CL", accent: "#2dd4bf" },
  TESTING: { category: "Data & Delivery", level: 74, icon: "T", accent: "#f59e0b" },
  TOOL: { category: "Data & Delivery", level: 72, icon: "TL", accent: "#a78bfa" },
  OTHER: { category: "Backend Development", level: 70, icon: "S", accent: "#fb7185" }
};

export function SkillsPage() {
  const technologies = useQuery({
    queryKey: ["public-technologies", "skills"],
    queryFn: getPublicTechnologies
  });
  const grouped = useMemo(() => groupByCategory(technologies.data ?? []), [technologies.data]);
  const coreSkills = (technologies.data ?? []).filter((technology) => technology.core).slice(0, 6);

  return (
    <section className="public-index skills-page technical-skills-page">
      <div className="skills-title-block">
        <p className="eyebrow">Skills</p>
        <h1>Technical Skills</h1>
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

      <div className="technical-skill-stack">
        {grouped.map(([category, items]) => (
          <article className="technical-skill-section" key={category}>
            <div className="technical-skill-heading">
              <span aria-hidden="true" />
              <h2>{category}</h2>
            </div>
            <div className="technical-skill-grid">
              {items.map((technology) => (
                <Link
                  className="technical-skill-card"
                  key={technology.slug}
                  style={{ "--skill-accent": getSkillMeta(technology).accent } as CSSProperties}
                  to={`/technologies/${technology.slug}`}
                >
                  <span className="skill-icon-mark" aria-hidden="true">{getSkillMeta(technology).icon}</span>
                  <strong>{technology.name}</strong>
                  <span className="skill-progress" aria-label={`${technology.name} proficiency ${getSkillMeta(technology).level}%`}>
                    <span style={{ width: `${getSkillMeta(technology).level}%` }} />
                  </span>
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

function groupByCategory(technologies: Technology[]) {
  const active = technologies.filter((technology) => technology.status === "ACTIVE");
  const map = new Map<SkillCategory, Technology[]>();

  active.forEach((technology) => {
    const category = getSkillMeta(technology).category;
    const existing = map.get(category) ?? [];
    map.set(category, [...existing, technology]);
  });

  return categoryOrder
    .map((category) => [category, map.get(category) ?? []] as const)
    .filter(([, items]) => items.length > 0);
}

function getSkillMeta(technology: Technology) {
  return skillMeta[technology.slug] ?? fallbackMeta[technology.type];
}
