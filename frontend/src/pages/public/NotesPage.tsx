import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { listPublicNotes } from "../../services/content";
import { publicMediaAssetUrl, type EntityMediaAsset } from "../../services/media";

export function NotesPage() {
  const notesQuery = useQuery({
    queryKey: ["public-notes"],
    queryFn: listPublicNotes
  });

  return (
    <section className="public-index">
      <div className="section-heading">
        <p className="eyebrow">Blog</p>
        <h1>Technical writing</h1>
      </div>
      {notesQuery.isError && <p className="form-error">Blog posts could not be loaded.</p>}
      {notesQuery.isLoading ? (
        <p className="muted">Loading blog posts...</p>
      ) : !notesQuery.data?.length ? (
        <p className="muted">Published blog posts will appear here.</p>
      ) : (
        <div className="public-grid">
          {notesQuery.data.map((note) => (
            <article className="public-card" key={note.id}>
              <PublicMediaImage asset={preferredMedia(note.media, "CONTENT_IMAGE")} className="public-card-media" />
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

function preferredMedia(media: EntityMediaAsset[], usageType: EntityMediaAsset["usageType"]) {
  return media.find((asset) => asset.usageType === usageType && asset.contentType.startsWith("image/")) ?? null;
}

function PublicMediaImage({
  asset,
  className
}: {
  asset: EntityMediaAsset | null;
  className: string;
}) {
  if (!asset) return null;
  return <img className={className} src={publicMediaAssetUrl(asset.mediaAssetId)} alt={asset.altText || asset.title} />;
}
