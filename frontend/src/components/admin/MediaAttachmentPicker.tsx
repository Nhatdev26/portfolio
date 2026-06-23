import { useEffect, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";

import {
  fetchMediaAssetObjectUrl,
  listMediaAssets,
  type MediaAsset,
  type MediaSelection,
  type MediaUsageType
} from "../../services/media";

export type MediaAttachmentSlot = {
  usageType: MediaUsageType;
  label: string;
  helper: string;
  multiple?: boolean;
};

export function MediaAttachmentPicker({
  token,
  slots,
  value,
  onChange
}: {
  token: string;
  slots: MediaAttachmentSlot[];
  value: MediaSelection;
  onChange: (value: MediaSelection) => void;
}) {
  const [query, setQuery] = useState("");
  const mediaQuery = useQuery({
    queryKey: ["media-assets", "picker"],
    queryFn: () => listMediaAssets(token)
  });
  const readyImageAssets = useMemo(
    () =>
      (mediaQuery.data ?? [])
        .filter((asset) => asset.status === "READY" && asset.contentType.startsWith("image/"))
        .filter((asset) => {
          const needle = query.trim().toLowerCase();
          if (!needle) return true;
          return `${asset.title} ${asset.originalFilename} ${asset.altText ?? ""} ${asset.caption ?? ""}`
            .toLowerCase()
            .includes(needle);
        }),
    [mediaQuery.data, query]
  );
  const selectedIds = new Set(slots.flatMap((slot) => value[slot.usageType] ?? []));
  const selectedAssets = (mediaQuery.data ?? []).filter((asset) => selectedIds.has(asset.id));

  function toggle(slot: MediaAttachmentSlot, assetId: number) {
    const current = value[slot.usageType] ?? [];
    const exists = current.includes(assetId);
    const nextIds = slot.multiple
      ? exists
        ? current.filter((id) => id !== assetId)
        : [...current, assetId]
      : exists
        ? []
        : [assetId];
    onChange({ ...value, [slot.usageType]: nextIds });
  }

  return (
    <fieldset className="nested-fieldset media-attachment-picker">
      <legend>Media attachments</legend>
      <div className="media-picker-header">
        <div>
          <p className="muted">Attach READY image assets from the Media Library.</p>
          <div className="media-picker-slots">
            {slots.map((slot) => (
              <span key={slot.usageType}>
                <strong>{slot.label}</strong>
                {slot.helper}
              </span>
            ))}
          </div>
        </div>
        <label>
          Search media
          <input
            placeholder="Title, filename, alt text"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>
      </div>

      {mediaQuery.isError && <p className="form-error">Media library could not be loaded.</p>}
      {mediaQuery.isLoading && <p className="muted">Loading media assets...</p>}

      {selectedAssets.length > 0 && (
        <div className="selected-media-strip" aria-label="Selected media">
          {selectedAssets.map((asset) => (
            <article key={asset.id}>
              <AdminMediaPreview asset={asset} token={token} />
              <strong>{asset.title}</strong>
              <small>{selectedUsageLabels(asset.id, slots, value).join(", ")}</small>
            </article>
          ))}
        </div>
      )}

      {!mediaQuery.isLoading && readyImageAssets.length === 0 ? (
        <div className="media-empty compact-empty">
          <strong>No READY image assets</strong>
          <span>Upload an image in the Media Library, then return here to attach it.</span>
        </div>
      ) : (
        <div className="media-picker-grid">
          {readyImageAssets.map((asset) => (
            <article className="media-picker-card" key={asset.id}>
              <AdminMediaPreview asset={asset} token={token} />
              <div>
                <strong>{asset.title}</strong>
                <small>{asset.visibility} · {asset.originalFilename}</small>
              </div>
              <div className="media-picker-actions">
                {slots.map((slot) => {
                  const isSelected = (value[slot.usageType] ?? []).includes(asset.id);
                  return (
                    <button
                      className={isSelected ? "secondary-button selected" : "secondary-button"}
                      key={slot.usageType}
                      type="button"
                      onClick={() => toggle(slot, asset.id)}
                    >
                      {isSelected ? "Remove" : slot.multiple ? `Add ${slot.label}` : `Use as ${slot.label}`}
                    </button>
                  );
                })}
              </div>
            </article>
          ))}
        </div>
      )}
    </fieldset>
  );
}

function selectedUsageLabels(assetId: number, slots: MediaAttachmentSlot[], value: MediaSelection) {
  return slots
    .filter((slot) => (value[slot.usageType] ?? []).includes(assetId))
    .map((slot) => slot.label);
}

function AdminMediaPreview({ asset, token }: { asset: MediaAsset; token: string }) {
  const objectUrl = useAdminObjectUrl(token, asset);
  if (objectUrl) {
    return <img className="media-preview-image" src={objectUrl} alt={asset.altText || asset.title} />;
  }
  return (
    <span className="media-file-preview">
      <strong>{asset.contentType.split("/")[1]?.toUpperCase() ?? "FILE"}</strong>
      <small>{asset.originalFilename}</small>
    </span>
  );
}

function useAdminObjectUrl(token: string, asset: MediaAsset) {
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
