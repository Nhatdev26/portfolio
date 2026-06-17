import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { listPublicNotes } from "../../services/content";

export function NotesPage() {
  const notesQuery = useQuery({
    queryKey: ["public-notes"],
    queryFn: listPublicNotes
  });

  return (
    <section className="public-index">
      <div className="section-heading">
        <p className="eyebrow">Technical writing</p>
        <h1>Notes</h1>
      </div>
      {notesQuery.isError && <p className="form-error">Notes could not be loaded.</p>}
      {notesQuery.isLoading ? (
        <p className="muted">Loading notes...</p>
      ) : !notesQuery.data?.length ? (
        <p className="muted">Published notes will appear here.</p>
      ) : (
        <div className="public-grid">
          {notesQuery.data.map((note) => (
            <article className="public-card" key={note.id}>
              <p className="card-meta">{note.category?.name ?? "Uncategorized"} · {note.readingMinutes} min</p>
              <h2><Link to={`/notes/${note.slug}`}>{note.title}</Link></h2>
              <p>{note.excerpt}</p>
              <div className="chip-row">
                {note.tags.map((tag) => <span className="text-chip" key={tag.slug}>{tag.name}</span>)}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
