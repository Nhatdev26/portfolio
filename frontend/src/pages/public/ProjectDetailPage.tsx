import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { getPublicProject } from "../../services/content";
import { publicMediaAssetUrl, type EntityMediaAsset } from "../../services/media";

export function ProjectDetailPage() {
  const { slug } = useParams();
  const projectQuery = useQuery({
    enabled: Boolean(slug),
    queryKey: ["public-project", slug],
    queryFn: () => getPublicProject(slug as string)
  });
  const project = projectQuery.data;

  return (
    <section className="public-detail">
      {projectQuery.isLoading && <p className="muted">Loading project...</p>}
      {projectQuery.isError && <p className="form-error">Project could not be loaded.</p>}
      {project && (
        <>
          <p className="eyebrow">{project.projectType} · {project.projectStatus}</p>
          <h1>{project.title}</h1>
          <p className="lead-text">{project.summary}</p>
          <PublicMediaFigure asset={preferredMedia(project.media, "COVER_IMAGE")} />
          <div className="chip-row">
            {project.technologies.map((technology) => (
              <Link className="text-chip" key={technology.slug} to={`/technologies/${technology.slug}`}>
                {technology.name}
              </Link>
            ))}
            {project.tags.map((tag) => <span className="text-chip" key={tag.slug}>{tag.name}</span>)}
          </div>
          <PublicMediaGallery
            media={project.media.filter((asset) => asset.usageType === "SCREENSHOT" && asset.contentType.startsWith("image/"))}
            title="Screenshots"
          />
          <div className="article-body">
            <Section title="Role" text={project.role} />
            <Section title="Description" text={project.description} />
            <Section title="Problem" text={project.problemStatement} />
            <Section title="Solution" text={project.solutionOverview} />
            <Section title="Backend highlights" text={project.backendHighlights} />
            <Section title="Frontend highlights" text={project.frontendHighlights} />
            <Section title="Architecture notes" text={project.architectureNotes} />
          </div>
          {(project.sourceUrl || project.demoUrl) && (
            <div className="hero-links compact-links">
              {project.sourceUrl && <a href={project.sourceUrl}>Source</a>}
              {project.demoUrl && <a href={project.demoUrl}>Demo</a>}
            </div>
          )}
          {project.notes.length > 0 && (
            <div className="related-list">
              <h2>Related notes</h2>
              {project.notes.map((note) => (
                <Link key={note.id} to={`/notes/${note.slug}`}>{note.title}</Link>
              ))}
            </div>
          )}
        </>
      )}
    </section>
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

function Section({ title, text }: { title: string; text: string | null }) {
  if (!text) return null;
  return (
    <section>
      <h2>{title}</h2>
      {text.split("\n").filter(Boolean).map((paragraph) => <p key={paragraph}>{paragraph}</p>)}
    </section>
  );
}
