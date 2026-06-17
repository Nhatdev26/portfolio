import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { listPublicProjects } from "../../services/content";

export function ProjectsPage() {
  const projectsQuery = useQuery({
    queryKey: ["public-projects"],
    queryFn: listPublicProjects
  });

  return (
    <section className="public-index">
      <div className="section-heading">
        <p className="eyebrow">Selected work</p>
        <h1>Projects</h1>
      </div>
      {projectsQuery.isError && <p className="form-error">Projects could not be loaded.</p>}
      {projectsQuery.isLoading ? (
        <p className="muted">Loading projects...</p>
      ) : !projectsQuery.data?.length ? (
        <p className="muted">Published projects will appear here.</p>
      ) : (
        <div className="public-grid">
          {projectsQuery.data.map((project) => (
            <article className="public-card" key={project.id}>
              <p className="card-meta">{project.projectType} · {project.projectStatus}</p>
              <h2><Link to={`/projects/${project.slug}`}>{project.title}</Link></h2>
              <p>{project.summary}</p>
              <div className="chip-row">
                {project.technologies.map((technology) => (
                  <Link className="text-chip" key={technology.slug} to={`/technologies/${technology.slug}`}>
                    {technology.name}
                  </Link>
                ))}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
