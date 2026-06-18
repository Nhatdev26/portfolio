import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { useAuth } from "../../features/auth/AuthProvider";
import { archiveNote, listAdminNotes } from "../../services/content";

export function NoteListPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const notesQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-notes"],
    queryFn: () => listAdminNotes(requireToken(session?.accessToken))
  });
  const archiveMutation = useMutation({
    mutationFn: (id: number) => archiveNote(requireToken(session?.accessToken), id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-notes"] })
  });

  return (
    <section className="content-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Notes CMS</p>
          <h1>Technical notes</h1>
        </div>
        <Link className="button-link" to="/admin/notes/new">New note</Link>
      </div>
      {notesQuery.isError && <p className="form-error">Notes could not be loaded.</p>}
      {archiveMutation.isError && <p className="form-error">Note could not be archived.</p>}
      {notesQuery.isLoading ? (
        <p className="muted">Loading notes...</p>
      ) : !notesQuery.data?.length ? (
        <p className="muted">No notes yet.</p>
      ) : (
        <div className="data-region" aria-live="polite">
          <table className="data-table">
            <thead>
              <tr>
                <th>Note</th>
                <th>Category</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {notesQuery.data.map((note) => (
                <tr key={note.id}>
                  <td>
                    <strong>{note.title}</strong>
                    <span>{note.slug} · {note.language}</span>
                  </td>
                  <td>{note.category?.name ?? "None"}</td>
                  <td>{note.status}</td>
                  <td>
                    <div className="table-actions">
                      <Link to={`/admin/notes/${note.id}/edit`}>Edit</Link>
                      {note.id !== null && note.status !== "ARCHIVED" && (
                        <button type="button" onClick={() => archiveMutation.mutate(note.id as number)}>
                          Archive
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
