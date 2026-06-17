import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import { MediaAttachmentPicker, type MediaAttachmentSlot } from "../../components/admin/MediaAttachmentPicker";
import { useAuth } from "../../features/auth/AuthProvider";
import {
  emptyNote,
  getAdminNote,
  noteToPayload,
  saveNote,
  type ContentLanguage,
  type ContentStatus,
  type NotePayload
} from "../../services/content";
import {
  mediaSelectionFromEntity,
  syncMediaUsages,
  type MediaSelection
} from "../../services/media";
import { getAdminTaxonomy } from "../../services/taxonomy";

const languages: ContentLanguage[] = ["EN", "VI"];
const statuses: ContentStatus[] = ["DRAFT", "PUBLISHED", "ARCHIVED"];
const noteMediaSlots: MediaAttachmentSlot[] = [
  {
    usageType: "CONTENT_IMAGE",
    label: "content image",
    helper: "Primary visual for the blog post."
  },
  {
    usageType: "DIAGRAM",
    label: "diagram",
    helper: "Optional supporting diagrams.",
    multiple: true
  }
];
const noteMediaUsageTypes = noteMediaSlots.map((slot) => slot.usageType);

export function NoteFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [form, setForm] = useState<NotePayload>(() => emptyNote());
  const [mediaSelection, setMediaSelection] = useState<MediaSelection>({});
  const [notice, setNotice] = useState<string | null>(null);

  const taxonomyQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-taxonomy"],
    queryFn: () => getAdminTaxonomy(requireToken(session?.accessToken))
  });
  const noteQuery = useQuery({
    enabled: Boolean(session?.accessToken && id && id !== "new"),
    queryKey: ["admin-note", id],
    queryFn: () => getAdminNote(requireToken(session?.accessToken), id as string)
  });
  const saveMutation = useMutation({
    mutationFn: async ({ payload, media }: { payload: NotePayload; media: MediaSelection }) => {
      const token = requireToken(session?.accessToken);
      const note = await saveNote(token, payload);
      if (note.id !== null) {
        await syncMediaUsages(
          token,
          "TECHNICAL_NOTE",
          note.id,
          note.media,
          media,
          noteMediaUsageTypes
        );
      }
      return { media, note };
    },
    onSuccess: async ({ media, note }) => {
      setNotice("Note saved.");
      setForm(noteToPayload(note));
      setMediaSelection(media);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin-notes"] }),
        queryClient.invalidateQueries({ queryKey: ["admin-note", String(note.id)] }),
        queryClient.invalidateQueries({ queryKey: ["media-assets"] })
      ]);
      if (!id || id === "new") navigate(`/admin/notes/${note.id}/edit`, { replace: true });
    }
  });

  useEffect(() => {
    if (noteQuery.data) {
      setForm(noteToPayload(noteQuery.data));
      setMediaSelection(mediaSelectionFromEntity(noteQuery.data.media, noteMediaUsageTypes));
    }
  }, [noteQuery.data]);

  function update<K extends keyof NotePayload>(field: K, value: NotePayload[K]) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submit(status?: ContentStatus) {
    setNotice(null);
    saveMutation.mutate({ payload: status ? { ...form, status } : form, media: mediaSelection });
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    submit();
  }

  const categories = taxonomyQuery.data?.categories.filter((category) => category.status === "ACTIVE") ?? [];
  const technologies = taxonomyQuery.data?.technologies.filter((technology) => technology.status === "ACTIVE") ?? [];
  const tags = taxonomyQuery.data?.tags.filter((tag) => tag.status === "ACTIVE") ?? [];

  return (
    <section className="content-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Notes CMS</p>
          <h1>{form.id ? "Edit note" : "New note"}</h1>
        </div>
        <Link className="button-link secondary" to="/admin/notes">Back to notes</Link>
      </div>

      {(taxonomyQuery.isError || noteQuery.isError) && <p className="form-error">Note form data could not be loaded.</p>}
      {saveMutation.isError && <p className="form-error">Note could not be saved. Check category and SEO before publishing.</p>}
      {notice && <p className="form-success">{notice}</p>}

      <form className="profile-form cms-editor" onSubmit={handleSubmit}>
        <fieldset>
          <legend>Core content</legend>
          <div className="form-grid">
            <TextField label="Title" value={form.title} onChange={(value) => update("title", value)} required />
            <TextField label="Slug" value={form.slug} onChange={(value) => update("slug", value)} required />
            <SelectField label="Language" value={form.language} options={languages} onChange={(value) => update("language", value as ContentLanguage)} />
            <SelectField label="Status" value={form.status} options={statuses} onChange={(value) => update("status", value as ContentStatus)} />
            <label>
              Category
              <select
                value={form.categoryId ?? ""}
                onChange={(event) => update("categoryId", event.target.value ? Number(event.target.value) : null)}
              >
                <option value="">None</option>
                {categories.map((category) => category.id !== null && (
                  <option key={category.id} value={category.id}>{category.name}</option>
                ))}
              </select>
            </label>
            <NumberField label="Reading minutes" value={form.readingMinutes} onChange={(value) => update("readingMinutes", value)} />
            <NumberField label="Display order" value={form.displayOrder} onChange={(value) => update("displayOrder", value)} />
          </div>
          <TextArea label="Excerpt" value={form.excerpt} onChange={(value) => update("excerpt", value)} required />
          <TextArea label="Markdown content" value={form.content} onChange={(value) => update("content", value)} required rows={10} />
        </fieldset>

        <MediaAttachmentPicker
          token={requireToken(session?.accessToken)}
          slots={noteMediaSlots}
          value={mediaSelection}
          onChange={setMediaSelection}
        />

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
        </fieldset>

        <fieldset>
          <legend>SEO</legend>
          <TextField label="SEO title" value={form.seoTitle ?? ""} onChange={(value) => update("seoTitle", value)} />
          <TextArea label="SEO description" value={form.seoDescription ?? ""} onChange={(value) => update("seoDescription", value)} />
        </fieldset>

        <div className="form-actions">
          <button disabled={saveMutation.isPending} type="submit">Save draft</button>
          <button className="secondary-button" disabled={saveMutation.isPending} type="button" onClick={() => submit("PUBLISHED")}>
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
  return <label>{label}<input min={1} type="number" value={value} onChange={(event) => onChange(Number(event.target.value))} /></label>;
}

function TextArea({ label, value, onChange, required = false, rows = 4 }: { label: string; value: string; onChange: (value: string) => void; required?: boolean; rows?: number }) {
  return <label>{label}<textarea required={required} rows={rows} value={value} onChange={(event) => onChange(event.target.value)} /></label>;
}

function SelectField({ label, value, options, onChange }: { label: string; value: string; options: string[]; onChange: (value: string) => void }) {
  return <label>{label}<select value={value} onChange={(event) => onChange(event.target.value)}>{options.map((option) => <option key={option}>{option}</option>)}</select></label>;
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
