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
