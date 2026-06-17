import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import { useAuth } from "../../features/auth/AuthProvider";
import {
  emptyProject,
  getAdminProject,
  listAdminNotes,
  projectToPayload,
  saveProject,
  type ContentLanguage,
  type ContentStatus,
  type ProjectLifecycleStatus,
  type ProjectPayload,
  type ProjectType
} from "../../services/content";
import { getAdminTaxonomy } from "../../services/taxonomy";

const languages: ContentLanguage[] = ["EN", "VI"];
const contentStatuses: ContentStatus[] = ["DRAFT", "PUBLISHED", "ARCHIVED"];
const projectTypes: ProjectType[] = ["BACKEND", "FULL_STACK", "API", "INFRASTRUCTURE", "OTHER"];
const lifecycleStatuses: ProjectLifecycleStatus[] = [
  "PLANNED",
  "IN_PROGRESS",
  "COMPLETED",
  "MAINTAINED",
  "ARCHIVED"
];

export function ProjectFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [form, setForm] = useState<ProjectPayload>(() => emptyProject());
  const [notice, setNotice] = useState<string | null>(null);

  const taxonomyQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-taxonomy"],
    queryFn: () => getAdminTaxonomy(requireToken(session?.accessToken))
  });
  const notesQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-notes"],
    queryFn: () => listAdminNotes(requireToken(session?.accessToken))
  });
  const projectQuery = useQuery({
    enabled: Boolean(session?.accessToken && id && id !== "new"),
    queryKey: ["admin-project", id],
    queryFn: () => getAdminProject(requireToken(session?.accessToken), id as string)
  });

  const saveMutation = useMutation({
    mutationFn: (payload: ProjectPayload) => saveProject(requireToken(session?.accessToken), payload),
    onSuccess: async (project) => {
      setNotice("Project saved.");
      setForm(projectToPayload(project));
      await queryClient.invalidateQueries({ queryKey: ["admin-projects"] });
      if (!id || id === "new") navigate(`/admin/projects/${project.id}/edit`, { replace: true });
    }
  });

  useEffect(() => {
    if (projectQuery.data) setForm(projectToPayload(projectQuery.data));
  }, [projectQuery.data]);

  function submit(event: FormEvent<HTMLFormElement>, status?: ContentStatus) {
    event.preventDefault();
    setNotice(null);
    saveMutation.mutate(status ? { ...form, contentStatus: status } : form);
  }

  function update<K extends keyof ProjectPayload>(field: K, value: ProjectPayload[K]) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  const technologies = taxonomyQuery.data?.technologies.filter((technology) => technology.status === "ACTIVE") ?? [];
  const tags = taxonomyQuery.data?.tags.filter((tag) => tag.status === "ACTIVE") ?? [];
  const notes = notesQuery.data?.filter((note) => note.status === "PUBLISHED") ?? [];

  return (
    <section className="content-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Projects CMS</p>
          <h1>{form.id ? "Edit project" : "New project"}</h1>
        </div>
        <Link className="button-link secondary" to="/admin/projects">Back to projects</Link>
      </div>

      {(taxonomyQuery.isError || notesQuery.isError || projectQuery.isError) && (
        <p className="form-error">Project form data could not be loaded.</p>
      )}
      {saveMutation.isError && <p className="form-error">Project could not be saved. Check required publish fields.</p>}
      {notice && <p className="form-success">{notice}</p>}

      <form className="profile-form cms-editor" onSubmit={(event) => submit(event)}>
        <fieldset>
          <legend>Core content</legend>
          <div className="form-grid">
            <TextField label="Title" value={form.title} onChange={(value) => update("title", value)} required />
            <TextField label="Slug" value={form.slug} onChange={(value) => update("slug", value)} required />
            <SelectField label="Language" value={form.language} options={languages} onChange={(value) => update("language", value as ContentLanguage)} />
            <SelectField label="Content status" value={form.contentStatus} options={contentStatuses} onChange={(value) => update("contentStatus", value as ContentStatus)} />
            <SelectField label="Project type" value={form.projectType} options={projectTypes} onChange={(value) => update("projectType", value as ProjectType)} />
            <SelectField label="Lifecycle" value={form.projectStatus} options={lifecycleStatuses} onChange={(value) => update("projectStatus", value as ProjectLifecycleStatus)} />
            <TextField label="Role" value={form.role} onChange={(value) => update("role", value)} required />
            <NumberField label="Display order" value={form.displayOrder} onChange={(value) => update("displayOrder", value)} />
          </div>
          <TextArea label="Summary" value={form.summary} onChange={(value) => update("summary", value)} required />
          <TextArea label="Description" value={form.description ?? ""} onChange={(value) => update("description", value)} />
        </fieldset>

        <fieldset>
          <legend>Case study</legend>
          <TextArea label="Problem statement" value={form.problemStatement ?? ""} onChange={(value) => update("problemStatement", value)} />
          <TextArea label="Solution overview" value={form.solutionOverview ?? ""} onChange={(value) => update("solutionOverview", value)} />
          <TextArea label="Backend highlights" value={form.backendHighlights ?? ""} onChange={(value) => update("backendHighlights", value)} />
          <TextArea label="Frontend highlights" value={form.frontendHighlights ?? ""} onChange={(value) => update("frontendHighlights", value)} />
          <TextArea label="Architecture notes" value={form.architectureNotes ?? ""} onChange={(value) => update("architectureNotes", value)} />
        </fieldset>

        <fieldset>
          <legend>Relations</legend>
          <CheckboxGroup
            label="Technologies"
            items={technologies.map((technology) => ({ id: technology.id, label: technology.name }))}
            selectedIds={form.technologyIds}
            onChange={(technologyIds) => update("technologyIds", technologyIds)}
          />
          <CheckboxGroup
            label="Tags"
            items={tags.map((tag) => ({ id: tag.id, label: tag.name }))}
            selectedIds={form.tagIds}
            onChange={(tagIds) => update("tagIds", tagIds)}
          />
          <CheckboxGroup
            label="Related notes"
            items={notes.map((note) => ({ id: note.id, label: note.title }))}
            selectedIds={form.noteIds}
            onChange={(noteIds) => update("noteIds", noteIds)}
          />
        </fieldset>

        <fieldset>
          <legend>Links and SEO</legend>
          <div className="form-grid">
            <TextField label="Source URL" value={form.sourceUrl ?? ""} onChange={(value) => update("sourceUrl", value)} />
            <TextField label="Demo URL" value={form.demoUrl ?? ""} onChange={(value) => update("demoUrl", value)} />
          </div>
          <TextField label="SEO title" value={form.seoTitle ?? ""} onChange={(value) => update("seoTitle", value)} />
          <TextArea label="SEO description" value={form.seoDescription ?? ""} onChange={(value) => update("seoDescription", value)} />
        </fieldset>

        <div className="form-actions">
          <button disabled={saveMutation.isPending} type="submit">Save draft</button>
          <button
            className="secondary-button"
            disabled={saveMutation.isPending}
            type="button"
            onClick={(event) => submit(event as unknown as FormEvent<HTMLFormElement>, "PUBLISHED")}
          >
            Publish
          </button>
        </div>
      </form>
    </section>
  );
}

function CheckboxGroup({
  label,
  items,
  selectedIds,
  onChange
}: {
  label: string;
  items: { id: number | null; label: string }[];
  selectedIds: number[];
  onChange: (ids: number[]) => void;
}) {
  return (
    <fieldset className="nested-fieldset">
      <legend>{label}</legend>
      <div className="checkbox-grid">
        {items.length === 0 ? (
          <p className="muted">No active records.</p>
        ) : (
          items.map((item) => item.id !== null && (
            <label key={item.id}>
              <input
                checked={selectedIds.includes(item.id)}
                type="checkbox"
                onChange={(event) =>
                  onChange(event.target.checked
                    ? [...selectedIds, item.id as number]
                    : selectedIds.filter((id) => id !== item.id))}
              />
              {item.label}
            </label>
          ))
        )}
      </div>
    </fieldset>
  );
}

function TextField({ label, value, onChange, required = false }: { label: string; value: string; onChange: (value: string) => void; required?: boolean }) {
  return <label>{label}<input required={required} value={value} onChange={(event) => onChange(event.target.value)} /></label>;
}

function NumberField({ label, value, onChange }: { label: string; value: number; onChange: (value: number) => void }) {
  return <label>{label}<input type="number" value={value} onChange={(event) => onChange(Number(event.target.value))} /></label>;
}

function TextArea({ label, value, onChange, required = false }: { label: string; value: string; onChange: (value: string) => void; required?: boolean }) {
  return <label>{label}<textarea required={required} rows={4} value={value} onChange={(event) => onChange(event.target.value)} /></label>;
}

function SelectField({ label, value, options, onChange }: { label: string; value: string; options: string[]; onChange: (value: string) => void }) {
  return <label>{label}<select value={value} onChange={(event) => onChange(event.target.value)}>{options.map((option) => <option key={option}>{option}</option>)}</select></label>;
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
