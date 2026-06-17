import { adminApi, publicApi } from "./apiClient";
import type { Category, Tag, Technology } from "./taxonomy";

export type ContentLanguage = "EN" | "VI";
export type ContentStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";
export type ProjectType = "BACKEND" | "FULL_STACK" | "API" | "INFRASTRUCTURE" | "OTHER";
export type ProjectLifecycleStatus =
  | "PLANNED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "MAINTAINED"
  | "ARCHIVED";

export type NoteSummary = {
  id: number;
  title: string;
  slug: string;
  language: ContentLanguage;
  excerpt: string;
  publishedAt: string | null;
};

export type Project = {
  id: number | null;
  title: string;
  slug: string;
  language: ContentLanguage;
  summary: string;
  description: string | null;
  role: string;
  projectType: ProjectType;
  projectStatus: ProjectLifecycleStatus;
  contentStatus: ContentStatus;
  problemStatement: string | null;
  solutionOverview: string | null;
  backendHighlights: string | null;
  frontendHighlights: string | null;
  architectureNotes: string | null;
  sourceUrl: string | null;
  demoUrl: string | null;
  seoTitle: string | null;
  seoDescription: string | null;
  publishedAt: string | null;
  displayOrder: number;
  technologies: Technology[];
  tags: Tag[];
  notes: NoteSummary[];
};

export type Note = {
  id: number | null;
  title: string;
  slug: string;
  language: ContentLanguage;
  excerpt: string;
  content: string;
  category: Category | null;
  status: ContentStatus;
  seoTitle: string | null;
  seoDescription: string | null;
  readingMinutes: number;
  publishedAt: string | null;
  displayOrder: number;
  technologies: Technology[];
  tags: Tag[];
};

export type ProjectPayload = Omit<Project, "technologies" | "tags" | "notes" | "publishedAt"> & {
  technologyIds: number[];
  tagIds: number[];
  noteIds: number[];
};

export type NotePayload = Omit<Note, "category" | "technologies" | "tags" | "publishedAt"> & {
  categoryId: number | null;
  technologyIds: number[];
  tagIds: number[];
};

export function emptyProject(): ProjectPayload {
  return {
    id: null,
    title: "",
    slug: "",
    language: "EN",
    summary: "",
    description: "",
    role: "",
    projectType: "FULL_STACK",
    projectStatus: "COMPLETED",
    contentStatus: "DRAFT",
    problemStatement: "",
    solutionOverview: "",
    backendHighlights: "",
    frontendHighlights: "",
    architectureNotes: "",
    sourceUrl: "",
    demoUrl: "",
    seoTitle: "",
    seoDescription: "",
    displayOrder: 0,
    technologyIds: [],
    tagIds: [],
    noteIds: []
  };
}

export function emptyNote(): NotePayload {
  return {
    id: null,
    title: "",
    slug: "",
    language: "EN",
    excerpt: "",
    content: "",
    categoryId: null,
    status: "DRAFT",
    seoTitle: "",
    seoDescription: "",
    readingMinutes: 1,
    displayOrder: 0,
    technologyIds: [],
    tagIds: []
  };
}

export function projectToPayload(project: Project): ProjectPayload {
  return {
    ...project,
    description: project.description ?? "",
    problemStatement: project.problemStatement ?? "",
    solutionOverview: project.solutionOverview ?? "",
    backendHighlights: project.backendHighlights ?? "",
    frontendHighlights: project.frontendHighlights ?? "",
    architectureNotes: project.architectureNotes ?? "",
    sourceUrl: project.sourceUrl ?? "",
    demoUrl: project.demoUrl ?? "",
    seoTitle: project.seoTitle ?? "",
    seoDescription: project.seoDescription ?? "",
    technologyIds: project.technologies.map((technology) => technology.id).filter((id): id is number => id !== null),
    tagIds: project.tags.map((tag) => tag.id).filter((id): id is number => id !== null),
    noteIds: project.notes.map((note) => note.id)
  };
}

export function noteToPayload(note: Note): NotePayload {
  return {
    ...note,
    seoTitle: note.seoTitle ?? "",
    seoDescription: note.seoDescription ?? "",
    categoryId: note.category?.id ?? null,
    technologyIds: note.technologies.map((technology) => technology.id).filter((id): id is number => id !== null),
    tagIds: note.tags.map((tag) => tag.id).filter((id): id is number => id !== null)
  };
}

export function listAdminProjects(accessToken: string) {
  return adminApi.get<Project[]>("/api/admin/projects", accessToken);
}

export function getAdminProject(accessToken: string, id: string) {
  return adminApi.get<Project>(`/api/admin/projects/${id}`, accessToken);
}

export function saveProject(accessToken: string, project: ProjectPayload) {
  return project.id
    ? adminApi.put<Project>(`/api/admin/projects/${project.id}`, accessToken, project)
    : adminApi.post<Project>("/api/admin/projects", accessToken, project);
}

export function archiveProject(accessToken: string, id: number) {
  return adminApi.patch<Project>(`/api/admin/projects/${id}/archive`, accessToken);
}

export function listAdminNotes(accessToken: string) {
  return adminApi.get<Note[]>("/api/admin/notes", accessToken);
}

export function getAdminNote(accessToken: string, id: string) {
  return adminApi.get<Note>(`/api/admin/notes/${id}`, accessToken);
}

export function saveNote(accessToken: string, note: NotePayload) {
  return note.id
    ? adminApi.put<Note>(`/api/admin/notes/${note.id}`, accessToken, note)
    : adminApi.post<Note>("/api/admin/notes", accessToken, note);
}

export function archiveNote(accessToken: string, id: number) {
  return adminApi.patch<Note>(`/api/admin/notes/${id}/archive`, accessToken);
}

export function listPublicProjects() {
  return publicApi.get<Project[]>("/public/projects");
}

export function getPublicProject(slug: string, language: ContentLanguage = "EN") {
  return publicApi.get<Project>(`/public/projects/${slug}?language=${language}`);
}

export function listPublicNotes() {
  return publicApi.get<Note[]>("/public/notes");
}

export function getPublicNote(slug: string, language: ContentLanguage = "EN") {
  return publicApi.get<Note>(`/public/notes/${slug}?language=${language}`);
}
