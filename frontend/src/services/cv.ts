import { adminApi } from "./apiClient";
import type { ContentLanguage } from "./content";

export type CvFileStatus = "DRAFT" | "ACTIVE" | "ARCHIVED";

export type CvFile = {
  id: number;
  language: ContentLanguage;
  targetRole: string;
  version: string;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  status: CvFileStatus;
  uploadedAt: string;
  activatedAt: string | null;
};

export function listAdminCvFiles(accessToken: string) {
  return adminApi.get<CvFile[]>("/api/admin/cv-files", accessToken);
}

export function uploadCvFile(
  accessToken: string,
  payload: {
    file: File;
    language: ContentLanguage;
    targetRole: string;
    version: string;
  }
) {
  const form = new FormData();
  form.append("file", payload.file);
  form.append("language", payload.language);
  form.append("targetRole", payload.targetRole);
  form.append("version", payload.version);
  return adminApi.post<CvFile>("/api/admin/cv-files", accessToken, form);
}

export function activateCvFile(accessToken: string, id: number) {
  return adminApi.patch<CvFile>(`/api/admin/cv-files/${id}/activate`, accessToken);
}

export function archiveCvFile(accessToken: string, id: number) {
  return adminApi.patch<CvFile>(`/api/admin/cv-files/${id}/archive`, accessToken);
}

export function publicCvDownloadUrl(language: ContentLanguage = "EN", targetRole = "backend-developer") {
  return `/public/cv/download?language=${language}&targetRole=${encodeURIComponent(targetRole)}`;
}

export async function activeCvExists(language: ContentLanguage = "EN", targetRole = "backend-developer") {
  const response = await fetch(publicCvDownloadUrl(language, targetRole), { method: "HEAD" });
  return response.ok;
}
