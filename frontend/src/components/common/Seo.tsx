import { useEffect } from "react";

const SITE_NAME = "Portfolio CMS";
const DEFAULT_TITLE = "Portfolio CMS";
const DEFAULT_DESCRIPTION =
  "A backend developer portfolio with projects, skills, technical writing, and a private CMS.";

type SeoProps = {
  title?: string | null;
  description?: string | null;
  canonicalPath?: string;
  imageUrl?: string | null;
  noIndex?: boolean;
  type?: "website" | "article" | "profile";
};

export function Seo({
  title,
  description,
  canonicalPath,
  imageUrl,
  noIndex = false,
  type = "website"
}: SeoProps) {
  useEffect(() => {
    const nextTitle = formatTitle(title);
    const nextDescription = normalizeDescription(description);
    const canonicalUrl = absoluteUrl(canonicalPath ?? window.location.pathname);
    const image = imageUrl ? absoluteUrl(imageUrl) : null;

    document.title = nextTitle;
    setMeta("description", nextDescription);
    setMeta("robots", noIndex ? "noindex,nofollow" : "index,follow");
    setMetaProperty("og:site_name", SITE_NAME);
    setMetaProperty("og:type", type);
    setMetaProperty("og:title", nextTitle);
    setMetaProperty("og:description", nextDescription);
    setMetaProperty("og:url", canonicalUrl);
    setMeta("twitter:card", image ? "summary_large_image" : "summary");
    setMeta("twitter:title", nextTitle);
    setMeta("twitter:description", nextDescription);
    setCanonical(canonicalUrl);

    if (image) {
      setMetaProperty("og:image", image);
      setMeta("twitter:image", image);
    } else {
      removeMetaProperty("og:image");
      removeMeta("twitter:image");
    }
  }, [canonicalPath, description, imageUrl, noIndex, title, type]);

  return null;
}

function formatTitle(title?: string | null) {
  const cleaned = title?.trim();
  if (!cleaned || cleaned === DEFAULT_TITLE) return DEFAULT_TITLE;
  return `${cleaned} | ${SITE_NAME}`;
}

function normalizeDescription(description?: string | null) {
  const cleaned = description?.replace(/\s+/g, " ").trim();
  return cleaned || DEFAULT_DESCRIPTION;
}

function absoluteUrl(pathOrUrl: string) {
  return new URL(pathOrUrl, window.location.origin).toString();
}

function setMeta(name: string, content: string) {
  const element =
    document.head.querySelector<HTMLMetaElement>(`meta[name="${name}"]`) ?? createMeta("name", name);
  element.content = content;
}

function removeMeta(name: string) {
  document.head.querySelector<HTMLMetaElement>(`meta[name="${name}"]`)?.remove();
}

function setMetaProperty(property: string, content: string) {
  const element =
    document.head.querySelector<HTMLMetaElement>(`meta[property="${property}"]`) ??
    createMeta("property", property);
  element.content = content;
}

function removeMetaProperty(property: string) {
  document.head.querySelector<HTMLMetaElement>(`meta[property="${property}"]`)?.remove();
}

function createMeta(attribute: "name" | "property", value: string) {
  const element = document.createElement("meta");
  element.setAttribute(attribute, value);
  document.head.appendChild(element);
  return element;
}

function setCanonical(href: string) {
  const element =
    document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]') ?? createCanonical();
  element.href = href;
}

function createCanonical() {
  const element = document.createElement("link");
  element.rel = "canonical";
  document.head.appendChild(element);
  return element;
}
