import { API_BASE_URL, adminApi } from "./apiClient";

export type MediaAssetStatus = "READY" | "DELETED" | "FAILED";
export type MediaVisibility = "PUBLIC" | "PRIVATE";
export type MediaEntityType = "PROJECT" | "TECHNICAL_NOTE" | "TECHNOLOGY" | "PROFILE";
export type MediaUsageType =
  | "COVER_IMAGE"
  | "THUMBNAIL"
  | "SCREENSHOT"
  | "DIAGRAM"
  | "CONTENT_IMAGE"
  | "OG_IMAGE"
  | "ICON"
  | "AVATAR";

export type MediaUsage = {
  id: number;
  entityType: MediaEntityType;
  entityId: number;
  usageType: MediaUsageType;
  createdAt: string;
};

export type MediaAsset = {
  id: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  title: string;
  altText: string | null;
  caption: string | null;
  status: MediaAssetStatus;
  visibility: MediaVisibility;
  uploadedAt: string;
  usages: MediaUsage[];
};

export type EntityMediaAsset = {
  usageId: number;
  mediaAssetId: number;
  usageType: MediaUsageType;
  originalFilename: string;
  contentType: string;
  title: string;
  altText: string | null;
  caption: string | null;
  status: MediaAssetStatus;
  visibility: MediaVisibility;
  uploadedAt: string;
};

export type MediaSelection = Partial<Record<MediaUsageType, number[]>>;

export type MediaAssetUpdateInput = {
  title: string;
  altText: string;
  caption: string;
  visibility: MediaVisibility;
};

export type UploadMediaAssetInput = {
  file: File;
  title: string;
  altText: string;
  caption: string;
  visibility: MediaVisibility;
};

export async function listMediaAssets(token: string) {
  return adminApi.get<MediaAsset[]>("/api/admin/media-assets", token);
}

export async function uploadMediaAsset(token: string, input: UploadMediaAssetInput) {
  const formData = new FormData();
  formData.set("file", input.file);
  if (input.title.trim()) formData.set("title", input.title.trim());
  if (input.altText.trim()) formData.set("altText", input.altText.trim());
  if (input.caption.trim()) formData.set("caption", input.caption.trim());
  formData.set("visibility", input.visibility);
  return adminApi.post<MediaAsset>("/api/admin/media-assets", token, formData);
}

export async function updateMediaAsset(token: string, id: number, input: MediaAssetUpdateInput) {
  return adminApi.put<MediaAsset>(`/api/admin/media-assets/${id}`, token, input);
}

export async function deleteMediaAsset(token: string, id: number) {
  return adminApi.delete<void>(`/api/admin/media-assets/${id}`, token);
}

export async function attachMediaUsage(
  token: string,
  mediaAssetId: number,
  input: {
    entityType: MediaEntityType;
    entityId: number;
    usageType: MediaUsageType;
  }
) {
  return adminApi.post<MediaUsage>(`/api/admin/media-assets/${mediaAssetId}/usages`, token, input);
}

export async function detachMediaUsage(token: string, mediaAssetId: number, usageId: number) {
  return adminApi.delete<void>(`/api/admin/media-assets/${mediaAssetId}/usages/${usageId}`, token);
}

export async function fetchMediaAssetObjectUrl(token: string, id: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/media-assets/${id}/content`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  if (!response.ok) {
    throw new Error("Media preview could not be loaded.");
  }
  return URL.createObjectURL(await response.blob());
}

export function publicMediaAssetUrl(id: number) {
  return `${API_BASE_URL}/public/media-assets/${id}/content`;
}

export function mediaSelectionFromEntity(media: EntityMediaAsset[], usageTypes: MediaUsageType[]): MediaSelection {
  const usageTypeSet = new Set(usageTypes);
  return media.reduce<MediaSelection>((selection, asset) => {
    if (!usageTypeSet.has(asset.usageType)) return selection;
    return {
      ...selection,
      [asset.usageType]: [...(selection[asset.usageType] ?? []), asset.mediaAssetId]
    };
  }, {});
}

export async function syncMediaUsages(
  token: string,
  entityType: MediaEntityType,
  entityId: number,
  currentMedia: EntityMediaAsset[],
  desiredSelection: MediaSelection,
  usageTypes: MediaUsageType[]
) {
  const usageTypeSet = new Set(usageTypes);
  const desiredPairs = new Set(
    usageTypes.flatMap((usageType) =>
      (desiredSelection[usageType] ?? []).map((mediaAssetId) => `${usageType}:${mediaAssetId}`)
    )
  );
  const currentPairs = new Set(
    currentMedia
      .filter((asset) => usageTypeSet.has(asset.usageType))
      .map((asset) => `${asset.usageType}:${asset.mediaAssetId}`)
  );

  await Promise.all(
    currentMedia
      .filter((asset) => usageTypeSet.has(asset.usageType))
      .filter((asset) => !desiredPairs.has(`${asset.usageType}:${asset.mediaAssetId}`))
      .map((asset) => detachMediaUsage(token, asset.mediaAssetId, asset.usageId))
  );

  await Promise.all(
    usageTypes.flatMap((usageType) =>
      (desiredSelection[usageType] ?? [])
        .filter((mediaAssetId) => !currentPairs.has(`${usageType}:${mediaAssetId}`))
        .map((mediaAssetId) => attachMediaUsage(token, mediaAssetId, { entityType, entityId, usageType }))
    )
  );
}
