import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { getPublicNote } from "../../services/content";

export function NoteDetailPage() {
  const { slug } = useParams();
  const noteQuery = useQuery({
    enabled: Boolean(slug),
    queryKey: ["public-note", slug],
    queryFn: () => getPublicNote(slug as string)
  });
  const note = noteQuery.data;

  return (
    <article className="public-detail">
      {noteQuery.isLoading && <p className="muted">Loading note...</p>}
      {noteQuery.isError && <p className="form-error">Note could not be loaded.</p>}
      {note && (
        <>
          <p className="eyebrow">{note.category?.name ?? "Technical note"} · {note.readingMinutes} min</p>
          <h1>{note.title}</h1>
          <p className="lead-text">{note.excerpt}</p>
          <div className="chip-row">
            {note.technologies.map((technology) => (
              <Link className="text-chip" key={technology.slug} to={`/technologies/${technology.slug}`}>
                {technology.name}
              </Link>
            ))}
            {note.tags.map((tag) => <span className="text-chip" key={tag.slug}>{tag.name}</span>)}
          </div>
          <div className="article-body markdown-lite">
            {note.content.split("\n").map((line, index) => {
              if (line.startsWith("## ")) return <h2 key={`${line}-${index}`}>{line.replace("## ", "")}</h2>;
              if (line.startsWith("# ")) return <h2 key={`${line}-${index}`}>{line.replace("# ", "")}</h2>;
              if (!line.trim()) return null;
              return <p key={`${line}-${index}`}>{line}</p>;
            })}
          </div>
        </>
      )}
    </article>
  );
}
