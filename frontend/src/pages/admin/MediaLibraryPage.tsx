import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useEffect, useMemo, useState } from "react";

import { useAuth } from "../../features/auth/AuthProvider";
import {
  deleteMediaAsset,
  fetchMediaAssetObjectUrl,
  listMediaAssets,
  updateMediaAsset,
  uploadMediaAsset,
  type MediaAsset,
  type MediaVisibility
} from "../../services/media";

const visibilityOptions: MediaVisibility[] = ["PRIVATE", "PUBLIC"];

export function MediaLibraryPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [visibilityFilter, setVisibilityFilter] = useState<"ALL" | MediaVisibility>("ALL");
  const [typeFilter, setTypeFilter] = useState<"ALL" | "IMAGE" | "DOCUMENT">("ALL");
  const [query, setQuery] = useState("");
  const [notice, setNotice] = useState<string | null>(null);

  const mediaQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["media-assets"],
    queryFn: () => listMediaAssets(requireToken(session?.accessToken))
  });

  const assets = mediaQuery.data ?? [];
  const filteredAssets = useMemo(
    () =>
      assets.filter((asset) => {
        const matchesVisibility = visibilityFilter === "ALL" || asset.visibility === visibilityFilter;
        const isImage = asset.contentType.startsWith("image/");
        const matchesType = typeFilter === "ALL" || (typeFilter === "IMAGE" ? isImage : !isImage);
        const haystack = `${asset.title} ${asset.originalFilename} ${asset.altText ?? ""} ${asset.caption ?? ""}`.toLowerCase();
        return matchesVisibility && matchesType && haystack.includes(query.trim().toLowerCase());
      }),
    [assets, query, typeFilter, visibilityFilter]
  );

  const selectedAsset = useMemo(
    () => filteredAssets.find((asset) => asset.id === selectedId) ?? filteredAssets[0] ?? null,
    [filteredAssets, selectedId]
  );
  const publicCount = assets.filter((asset) => asset.visibility === "PUBLIC").length;
  const privateCount = assets.filter((asset) => asset.visibility === "PRIVATE").length;
  const usedCount = assets.filter((asset) => asset.usages.length > 0).length;

  const uploadMutation = useMutation({
    mutationFn: (input: {
      file: File;
      title: string;
      altText: string;
      caption: string;
      visibility: MediaVisibility;
    }) => uploadMediaAsset(requireToken(session?.accessToken), input),
    onSuccess: async (asset) => {
      setSelectedId(asset.id);
      setNotice("Media uploaded and ready.");
      await queryClient.invalidateQueries({ queryKey: ["media-assets"] });
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, title, altText, caption, visibility }: {
      id: number;
      title: string;
      altText: string;
      caption: string;
      visibility: MediaVisibility;
    }) => updateMediaAsset(requireToken(session?.accessToken), id, { title, altText, caption, visibility }),
    onSuccess: async (asset) => {
      setSelectedId(asset.id);
      setNotice("Media metadata saved.");
      await queryClient.invalidateQueries({ queryKey: ["media-assets"] });
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteMediaAsset(requireToken(session?.accessToken), id),
    onSuccess: async () => {
      setSelectedId(null);
      setNotice("Media deleted.");
      await queryClient.invalidateQueries({ queryKey: ["media-assets"] });
    }
  });

  return (
    <section className="content-panel media-page">
      <div className="panel-heading media-heading">
        <div>
          <p className="eyebrow">Media Library</p>
          <h1>Media assets</h1>
          <p className="muted">Upload, curate, and protect reusable portfolio visuals.</p>
        </div>
        {mediaQuery.isFetching && <span className="status-chip">Syncing</span>}
      </div>

      <div className="media-metrics" aria-label="Media summary">
        <article>
          <span>Total assets</span>
          <strong>{assets.length}</strong>
        </article>
        <article>
          <span>Public</span>
          <strong>{publicCount}</strong>
        </article>
        <article>
          <span>Private</span>
          <strong>{privateCount}</strong>
        </article>
        <article>
          <span>In use</span>
          <strong>{usedCount}</strong>
        </article>
      </div>

      {notice && <p className="form-success">{notice}</p>}
      {mediaQuery.isError && <p className="form-error">Media assets could not be loaded.</p>}
      {uploadMutation.isError && <p className="form-error">Upload failed. Use JPEG, PNG, WebP, GIF, SVG, or PDF up to 10 MB.</p>}
      {updateMutation.isError && <p className="form-error">Media metadata could not be saved.</p>}
      {deleteMutation.isError && <p className="form-error">Media could not be deleted. Used media must stay attached.</p>}

        <UploadPanel
        isBusy={uploadMutation.isPending}
        onUpload={(input) => {
          setNotice(null);
          uploadMutation.mutate(input);
        }}
      />

      <div className="media-toolbar">
        <label>
          Search
          <input
            placeholder="Title, filename, alt text"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>
        <label>
          Visibility
          <select value={visibilityFilter} onChange={(event) => setVisibilityFilter(event.target.value as "ALL" | MediaVisibility)}>
            <option value="ALL">All visibility</option>
            {visibilityOptions.map((visibility) => (
              <option key={visibility} value={visibility}>{labelize(visibility)}</option>
            ))}
          </select>
        </label>
        <label>
          Type
          <select value={typeFilter} onChange={(event) => setTypeFilter(event.target.value as "ALL" | "IMAGE" | "DOCUMENT")}>
            <option value="ALL">All types</option>
            <option value="IMAGE">Images</option>
            <option value="DOCUMENT">Documents</option>
          </select>
        </label>
      </div>

      <div className="media-workspace">
        <MediaGrid
          assets={filteredAssets}
          isLoading={mediaQuery.isLoading}
          selectedId={selectedAsset?.id ?? null}
          token={requireToken(session?.accessToken)}
          onSelect={setSelectedId}
        />
        <MediaDetailPanel
          asset={selectedAsset}
          isBusy={updateMutation.isPending || deleteMutation.isPending}
          token={requireToken(session?.accessToken)}
          onSave={(input) => {
            if (!selectedAsset) return;
            setNotice(null);
            updateMutation.mutate({ id: selectedAsset.id, ...input });
          }}
          onDelete={() => {
            if (!selectedAsset) return;
            if (selectedAsset.usages.length > 0) {
              setNotice(null);
              return;
            }
            if (window.confirm(`Delete ${selectedAsset.title}? This keeps audit history but removes it from the library.`)) {
              setNotice(null);
              deleteMutation.mutate(selectedAsset.id);
            }
          }}
        />
      </div>
    </section>
  );
}

function UploadPanel({
  isBusy,
  onUpload
}: {
  isBusy: boolean;
  onUpload: (input: {
    file: File;
    title: string;
    altText: string;
    caption: string;
    visibility: MediaVisibility;
  }) => void;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState("");
  const [altText, setAltText] = useState("");
  const [caption, setCaption] = useState("");
  const [visibility, setVisibility] = useState<MediaVisibility>("PRIVATE");
  const localPreview = useLocalObjectUrl(file);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!file) return;
    onUpload({ file, title, altText, caption, visibility });
    setFile(null);
    setTitle("");
    setAltText("");
    setCaption("");
    setVisibility("PRIVATE");
  }

  return (
    <form className="media-upload-panel" onSubmit={submit}>
      <label className="media-dropzone">
        <input
          accept="image/jpeg,image/png,image/webp,image/gif,image/svg+xml,application/pdf,.jpg,.jpeg,.png,.webp,.gif,.svg,.pdf"
          type="file"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
        />
        <span className="media-dropzone-art" aria-hidden="true">
          <span />
          <span />
          <span />
        </span>
        <strong>{file ? file.name : "Choose media file"}</strong>
        <small>JPEG, PNG, WebP, GIF, SVG, or PDF up to 10 MB.</small>
      </label>
      <div className="media-upload-preview">
        {file && file.type.startsWith("image/") && localPreview ? (
          <img src={localPreview} alt={altText || title || file.name} />
        ) : (
          <div>
            <strong>{file ? shortType(file.type) : "Preview"}</strong>
            <span>{file ? formatBytes(file.size) : "No file selected"}</span>
          </div>
        )}
      </div>
      <div className="media-upload-fields">
        <label>
          Title
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Portfolio hero cover" />
        </label>
        <label>
          Alt text
          <input value={altText} onChange={(event) => setAltText(event.target.value)} placeholder="Descriptive image text" />
        </label>
        <label>
          Visibility
          <select value={visibility} onChange={(event) => setVisibility(event.target.value as MediaVisibility)}>
            <option value="PRIVATE">Private</option>
            <option value="PUBLIC">Public</option>
          </select>
        </label>
        <label className="wide-field">
          Caption
          <textarea rows={2} value={caption} onChange={(event) => setCaption(event.target.value)} placeholder="Optional caption for content reuse." />
        </label>
        <button disabled={isBusy || !file} type="submit">
          {isBusy ? "Uploading..." : "Upload asset"}
        </button>
      </div>
    </form>
  );
}

function MediaGrid({
  assets,
  isLoading,
  selectedId,
  token,
  onSelect
}: {
  assets: MediaAsset[];
  isLoading: boolean;
  selectedId: number | null;
  token: string;
  onSelect: (id: number) => void;
}) {
  if (isLoading) {
    return <p className="muted">Loading media assets...</p>;
  }
  if (assets.length === 0) {
    return (
      <div className="media-empty">
        <strong>No media found</strong>
        <span>Upload a first asset or clear filters.</span>
      </div>
    );
  }
  return (
    <div className="media-grid" aria-live="polite">
      {assets.map((asset) => (
        <button
          className={asset.id === selectedId ? "media-card selected" : "media-card"}
          key={asset.id}
          type="button"
          onClick={() => onSelect(asset.id)}
        >
          <MediaPreview asset={asset} token={token} />
          <span className="media-card-body">
            <strong>{asset.title}</strong>
            <small>{asset.originalFilename}</small>
            <span className="chip-row">
              <span className="status-chip quiet">{asset.visibility}</span>
              {asset.usages.length > 0 && <span className="status-chip">USED</span>}
            </span>
          </span>
        </button>
      ))}
    </div>
  );
}

function MediaDetailPanel({
  asset,
  isBusy,
  token,
  onSave,
  onDelete
}: {
  asset: MediaAsset | null;
  isBusy: boolean;
  token: string;
  onSave: (input: {
    title: string;
    altText: string;
    caption: string;
    visibility: MediaVisibility;
  }) => void;
  onDelete: () => void;
}) {
  const [title, setTitle] = useState("");
  const [altText, setAltText] = useState("");
  const [caption, setCaption] = useState("");
  const [visibility, setVisibility] = useState<MediaVisibility>("PRIVATE");

  useEffect(() => {
    setTitle(asset?.title ?? "");
    setAltText(asset?.altText ?? "");
    setCaption(asset?.caption ?? "");
    setVisibility(asset?.visibility ?? "PRIVATE");
  }, [asset]);

  if (!asset) {
    return (
      <aside className="media-detail-panel">
        <p className="eyebrow">Inspector</p>
        <h2>Select an asset</h2>
        <p className="muted">Choose a media card to inspect metadata, visibility, and usage protection.</p>
      </aside>
    );
  }

  const isUsed = asset.usages.length > 0;

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSave({ title, altText, caption, visibility });
  }

  return (
    <aside className="media-detail-panel">
      <div>
        <p className="eyebrow">Inspector</p>
        <h2>{asset.title}</h2>
        <p className="muted">{asset.originalFilename} · {formatBytes(asset.fileSize)}</p>
      </div>
      <div className="media-detail-preview">
        <MediaPreview asset={asset} token={token} />
      </div>
      <form className="profile-form media-editor-form" onSubmit={submit}>
        <label>
          Title
          <input required value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <label>
          Alt text
          <input value={altText} onChange={(event) => setAltText(event.target.value)} />
        </label>
        <label>
          Caption
          <textarea rows={3} value={caption} onChange={(event) => setCaption(event.target.value)} />
        </label>
        <label>
          Visibility
          <select value={visibility} onChange={(event) => setVisibility(event.target.value as MediaVisibility)}>
            <option value="PRIVATE">Private</option>
            <option value="PUBLIC">Public</option>
          </select>
        </label>
        <div className="form-actions">
          <button disabled={isBusy} type="submit">Save metadata</button>
          <button
            className="danger-button"
            disabled={isBusy || isUsed}
            type="button"
            onClick={onDelete}
            title={isUsed ? "Used media cannot be deleted." : "Delete media"}
          >
            Delete
          </button>
        </div>
      </form>
      <section className={isUsed ? "media-usage-panel active" : "media-usage-panel"}>
        <strong>{isUsed ? "Delete protected" : "No usages yet"}</strong>
        <span>{isUsed ? "This asset is attached to content and cannot be deleted." : "Unused assets can be deleted from this library."}</span>
        {asset.usages.map((usage) => (
          <p key={usage.id}>{labelize(usage.usageType)} · {labelize(usage.entityType)} #{usage.entityId}</p>
        ))}
      </section>
    </aside>
  );
}

function MediaPreview({ asset, token }: { asset: MediaAsset; token: string }) {
  const objectUrl = useAdminMediaObjectUrl(token, asset);
  if (asset.contentType.startsWith("image/") && objectUrl) {
    return <img className="media-preview-image" src={objectUrl} alt={asset.altText || asset.title} />;
  }
  return (
    <span className="media-file-preview">
      <strong>{shortType(asset.contentType)}</strong>
      <small>{formatBytes(asset.fileSize)}</small>
    </span>
  );
}

function useAdminMediaObjectUrl(token: string, asset: MediaAsset) {
  const [url, setUrl] = useState<string | null>(null);
  useEffect(() => {
    if (!asset.contentType.startsWith("image/")) {
      setUrl(null);
      return;
    }
    let isMounted = true;
    let nextUrl: string | null = null;
    fetchMediaAssetObjectUrl(token, asset.id)
      .then((value) => {
        nextUrl = value;
        if (isMounted) setUrl(value);
      })
      .catch(() => {
        if (isMounted) setUrl(null);
      });
    return () => {
      isMounted = false;
      if (nextUrl) URL.revokeObjectURL(nextUrl);
    };
  }, [asset.contentType, asset.id, token]);
  return url;
}

function useLocalObjectUrl(file: File | null) {
  const [url, setUrl] = useState<string | null>(null);
  useEffect(() => {
    if (!file) {
      setUrl(null);
      return;
    }
    const nextUrl = URL.createObjectURL(file);
    setUrl(nextUrl);
    return () => URL.revokeObjectURL(nextUrl);
  }, [file]);
  return url;
}

function formatBytes(bytes: number) {
  return new Intl.NumberFormat("en", {
    maximumFractionDigits: 1,
    style: "unit",
    unit: "megabyte"
  }).format(bytes / 1024 / 1024);
}

function shortType(contentType: string) {
  const [, subtype = "file"] = contentType.split("/");
  return subtype.replace("svg+xml", "svg").toUpperCase();
}

function labelize(value: string) {
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
