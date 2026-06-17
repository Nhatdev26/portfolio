import { adminApi, publicApi } from "./apiClient";

export type ProfileStatus = "DRAFT" | "ACTIVE" | "INACTIVE";
export type ProfileLanguage = "EN" | "VI";
export type ProfileContentStatus = "DRAFT" | "ACTIVE" | "INACTIVE";
export type SocialLinkPlatform = "GITHUB" | "LINKEDIN" | "EMAIL" | "PORTFOLIO" | "OTHER";
export type SocialLinkStatus = "ACTIVE" | "INACTIVE";

export type ProfileContent = {
  id: number | null;
  language: ProfileLanguage;
  headline: string;
  subheadline: string;
  shortBio: string;
  longBio: string;
  seoTitle: string;
  seoDescription: string;
  status: ProfileContentStatus;
};

export type SocialLink = {
  id: number | null;
  platform: SocialLinkPlatform;
  label: string;
  url: string;
  displayOrder: number;
  status: SocialLinkStatus;
};

export type AdminProfile = {
  id: number | null;
  displayName: string;
  email: string;
  location: string;
  primaryRole: string;
  careerDirection: string;
  mainTechFocus: string;
  status: ProfileStatus;
  contents: ProfileContent[];
  socialLinks: SocialLink[];
};

export type PublicProfile = {
  displayName: string;
  email: string;
  location: string | null;
  primaryRole: string;
  careerDirection: string | null;
  mainTechFocus: string | null;
  language: ProfileLanguage;
  headline: string;
  subheadline: string | null;
  shortBio: string | null;
  longBio: string | null;
  socialLinks: SocialLink[];
};

export function getAdminProfile(accessToken: string) {
  return adminApi.get<AdminProfile>("/api/admin/profile", accessToken);
}

export function saveAdminProfile(accessToken: string, profile: AdminProfile) {
  return adminApi.put<AdminProfile>("/api/admin/profile", accessToken, profile);
}

export function getPublicProfile(language: ProfileLanguage = "EN") {
  return publicApi.get<PublicProfile>(`/public/profile?language=${language}`);
}
