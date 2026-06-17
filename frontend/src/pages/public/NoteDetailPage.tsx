import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { getPublicNote } from "../../services/content";
import { publicMediaAssetUrl, type EntityMediaAsset } from "../../services/media";

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
      {noteQuery.isLoading && <p className="muted">Loading blog post...</p>}
      {noteQuery.isError && <p className="form-error">Blog post could not be loaded.</p>}
      {note && (
        <>
          <p className="eyebrow">{note.category?.name ?? "Blog"} · {note.readingMinutes} min</p>
          <h1>{note.title}</h1>
          <p className="lead-text">{note.excerpt}</p>
          <PublicMediaFigure asset={preferredMedia(note.media, "CONTENT_IMAGE")} />
          <div className="chip-row">
            {note.technologies.map((technology) => (
              <Link className="text-chip" key={technology.slug} to={`/technologies/${technology.slug}`}>
                {technology.name}
              </Link>
            ))}
            {note.tags.map((tag) => <span className="text-chip" key={tag.slug}>{tag.name}</span>)}
          </div>
          <PublicMediaGallery
            media={note.media.filter((asset) => asset.usageType === "DIAGRAM" && asset.contentType.startsWith("image/"))}
            title="Diagrams"
          />
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

function preferredMedia(media: EntityMediaAsset[], usageType: EntityMediaAsset["usageType"]) {
  return media.find((asset) => asset.usageType === usageType && asset.contentType.startsWith("image/")) ?? null;
}

function PublicMediaFigure({ asset }: { asset: EntityMediaAsset | null }) {
  if (!asset) return null;
  return (
    <figure className="public-media-figure">
      <img src={publicMediaAssetUrl(asset.mediaAssetId)} alt={asset.altText || asset.title} />
      {asset.caption && <figcaption>{asset.caption}</figcaption>}
    </figure>
  );
}

function PublicMediaGallery({ media, title }: { media: EntityMediaAsset[]; title: string }) {
  if (media.length === 0) return null;
  return (
    <section className="public-media-gallery" aria-label={title}>
      <h2>{title}</h2>
      <div>
        {media.map((asset) => (
          <figure key={asset.usageId}>
            <img src={publicMediaAssetUrl(asset.mediaAssetId)} alt={asset.altText || asset.title} />
            {asset.caption && <figcaption>{asset.caption}</figcaption>}
          </figure>
        ))}
      </div>
    </section>
  );
}
