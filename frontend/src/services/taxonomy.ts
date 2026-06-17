import { adminApi, publicApi } from "./apiClient";

export type TaxonomyStatus = "ACTIVE" | "ARCHIVED";
export type TechnologyType =
  | "LANGUAGE"
  | "FRAMEWORK"
  | "DATABASE"
  | "DEVOPS"
  | "CLOUD"
  | "TESTING"
  | "TOOL"
  | "OTHER";

export type Category = {
  id: number | null;
  name: string;
  slug: string;
  description: string | null;
  status: TaxonomyStatus;
  displayOrder: number;
};

export type Tag = {
  id: number | null;
  name: string;
  slug: string;
  status: TaxonomyStatus;
  displayOrder: number;
};

export type Technology = {
  id: number | null;
  name: string;
  slug: string;
  type: TechnologyType;
  status: TaxonomyStatus;
  description: string | null;
  howIUseIt: string | null;
  core: boolean;
  displayOrder: number;
};

export type SkillGroup = {
  id: number | null;
  name: string;
  slug: string;
  description: string | null;
  status: TaxonomyStatus;
  displayOrder: number;
  technologies: Technology[];
};

export type SkillGroupPayload = Omit<SkillGroup, "technologies"> & {
  technologyIds: number[];
};

export type TaxonomyAdmin = {
  categories: Category[];
  tags: Tag[];
  technologies: Technology[];
  skillGroups: SkillGroup[];
};

export function emptyCategory(): Category {
  return { id: null, name: "", slug: "", description: "", status: "ACTIVE", displayOrder: 0 };
}

export function emptyTag(): Tag {
  return { id: null, name: "", slug: "", status: "ACTIVE", displayOrder: 0 };
}

export function emptyTechnology(): Technology {
  return {
    id: null,
    name: "",
    slug: "",
    type: "FRAMEWORK",
    status: "ACTIVE",
    description: "",
    howIUseIt: "",
    core: false,
    displayOrder: 0
  };
}

export function emptySkillGroup(): SkillGroupPayload {
  return {
    id: null,
    name: "",
    slug: "",
    description: "",
    status: "ACTIVE",
    displayOrder: 0,
    technologyIds: []
  };
}

export function getAdminTaxonomy(accessToken: string) {
  return adminApi.get<TaxonomyAdmin>("/api/admin/taxonomy", accessToken);
}

export function saveCategory(accessToken: string, category: Category) {
  return category.id
    ? adminApi.put<Category>(`/api/admin/categories/${category.id}`, accessToken, category)
    : adminApi.post<Category>("/api/admin/categories", accessToken, category);
}

export function saveTag(accessToken: string, tag: Tag) {
  return tag.id
    ? adminApi.put<Tag>(`/api/admin/tags/${tag.id}`, accessToken, tag)
    : adminApi.post<Tag>("/api/admin/tags", accessToken, tag);
}

export function saveTechnology(accessToken: string, technology: Technology) {
  return technology.id
    ? adminApi.put<Technology>(`/api/admin/technologies/${technology.id}`, accessToken, technology)
    : adminApi.post<Technology>("/api/admin/technologies", accessToken, technology);
}

export function saveSkillGroup(accessToken: string, skillGroup: SkillGroupPayload) {
  return skillGroup.id
    ? adminApi.put<SkillGroup>(`/api/admin/skill-groups/${skillGroup.id}`, accessToken, skillGroup)
    : adminApi.post<SkillGroup>("/api/admin/skill-groups", accessToken, skillGroup);
}

export function archiveTaxonomyItem(
  accessToken: string,
  type: "categories" | "tags" | "technologies" | "skill-groups",
  id: number
) {
  return adminApi.patch<void>(`/api/admin/${type}/${id}/archive`, accessToken);
}

export function getPublicTechnologies() {
  return publicApi.get<Technology[]>("/public/technologies");
}

export function getPublicTechnology(slug: string) {
  return publicApi.get<Technology>(`/public/technologies/${slug}`);
}
