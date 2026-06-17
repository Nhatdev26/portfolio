import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { useAuth } from "../../features/auth/AuthProvider";
import { archiveProject, listAdminProjects } from "../../services/content";

export function ProjectListPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const projectsQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-projects"],
    queryFn: () => listAdminProjects(requireToken(session?.accessToken))
  });
  const archiveMutation = useMutation({
    mutationFn: (id: number) => archiveProject(requireToken(session?.accessToken), id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-projects"] })
  });

  return (
    <section className="content-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Projects CMS</p>
          <h1>Projects</h1>
        </div>
        <Link className="button-link" to="/admin/projects/new">New project</Link>
      </div>
      {projectsQuery.isError && <p className="form-error">Projects could not be loaded.</p>}
      {archiveMutation.isError && <p className="form-error">Project could not be archived.</p>}
      {projectsQuery.isLoading ? (
        <p className="muted">Loading projects...</p>
      ) : !projectsQuery.data?.length ? (
        <p className="muted">No projects yet.</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Project</th>
              <th>Status</th>
              <th>Technologies</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {projectsQuery.data.map((project) => (
              <tr key={project.id}>
                <td>
                  <strong>{project.title}</strong>
                  <span>{project.slug} · {project.language}</span>
                </td>
                <td>{project.contentStatus}</td>
                <td>{project.technologies.map((technology) => technology.name).join(", ") || "None"}</td>
                <td>
                  <div className="table-actions">
                    <Link to={`/admin/projects/${project.id}/edit`}>Edit</Link>
                    {project.id !== null && project.contentStatus !== "ARCHIVED" && (
                      <button type="button" onClick={() => archiveMutation.mutate(project.id as number)}>
                        Archive
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
