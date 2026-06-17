import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useEffect, useMemo, useState } from "react";

import { useAuth } from "../../features/auth/AuthProvider";
import {
  archiveTaxonomyItem,
  emptyCategory,
  emptySkillGroup,
  emptyTag,
  emptyTechnology,
  getAdminTaxonomy,
  saveCategory,
  saveSkillGroup,
  saveTag,
  saveTechnology,
  type Category,
  type SkillGroup,
  type SkillGroupPayload,
  type Tag,
  type TaxonomyStatus,
  type Technology,
  type TechnologyType
} from "../../services/taxonomy";

type Section = "categories" | "tags" | "technologies" | "skill-groups";

const statuses: TaxonomyStatus[] = ["ACTIVE", "ARCHIVED"];
const technologyTypes: TechnologyType[] = [
  "LANGUAGE",
  "FRAMEWORK",
  "DATABASE",
  "DEVOPS",
  "CLOUD",
  "TESTING",
  "TOOL",
  "OTHER"
];

const sectionLabels: Record<Section, string> = {
  categories: "Categories",
  tags: "Tags",
  technologies: "Technologies",
  "skill-groups": "Skill groups"
};

export function TaxonomyPage({ section }: { section: Section }) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [categoryForm, setCategoryForm] = useState<Category>(() => emptyCategory());
  const [tagForm, setTagForm] = useState<Tag>(() => emptyTag());
  const [technologyForm, setTechnologyForm] = useState<Technology>(() => emptyTechnology());
  const [skillGroupForm, setSkillGroupForm] = useState<SkillGroupPayload>(() => emptySkillGroup());
  const [notice, setNotice] = useState<string | null>(null);

  const taxonomyQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-taxonomy"],
    queryFn: () => getAdminTaxonomy(requireToken(session?.accessToken))
  });

  const saveMutation = useMutation({
    mutationFn: async () => {
      const token = requireToken(session?.accessToken);
      if (section === "categories") return saveCategory(token, categoryForm);
      if (section === "tags") return saveTag(token, tagForm);
      if (section === "technologies") return saveTechnology(token, technologyForm);
      return saveSkillGroup(token, skillGroupForm);
    },
    onSuccess: async () => {
      setNotice(`${sectionLabels[section]} saved.`);
      resetForm();
      await queryClient.invalidateQueries({ queryKey: ["admin-taxonomy"] });
    }
  });

  const archiveMutation = useMutation({
    mutationFn: (id: number) =>
      archiveTaxonomyItem(requireToken(session?.accessToken), section, id),
    onSuccess: async () => {
      setNotice(`${sectionLabels[section]} archived.`);
      await queryClient.invalidateQueries({ queryKey: ["admin-taxonomy"] });
    }
  });

  useEffect(() => {
    setNotice(null);
    resetForm();
  }, [section]);

  const rows = useMemo(() => {
    const data = taxonomyQuery.data;
    if (!data) return [];
    if (section === "categories") return data.categories;
    if (section === "tags") return data.tags;
    if (section === "technologies") return data.technologies;
    return data.skillGroups;
  }, [section, taxonomyQuery.data]);

  function resetForm() {
    setCategoryForm(emptyCategory());
    setTagForm(emptyTag());
    setTechnologyForm(emptyTechnology());
    setSkillGroupForm(emptySkillGroup());
  }

  function editRow(row: Category | Tag | Technology | SkillGroup) {
    setNotice(null);
    if (section === "categories") setCategoryForm(row as Category);
    if (section === "tags") setTagForm(row as Tag);
    if (section === "technologies") setTechnologyForm(row as Technology);
    if (section === "skill-groups") {
      const group = row as SkillGroup;
      setSkillGroupForm({
        id: group.id,
        name: group.name,
        slug: group.slug,
        description: group.description,
        status: group.status,
        displayOrder: group.displayOrder,
        technologyIds: group.technologies
          .map((technology) => technology.id)
          .filter((id): id is number => id !== null)
      });
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice(null);
    saveMutation.mutate();
  }

  const isBusy = saveMutation.isPending || archiveMutation.isPending;

  return (
    <section className="content-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Taxonomy CMS</p>
          <h1>{sectionLabels[section]}</h1>
        </div>
        {taxonomyQuery.isFetching && <span className="status-chip">Syncing</span>}
      </div>

      {taxonomyQuery.isError && <p className="form-error">Taxonomy data could not be loaded.</p>}
      {notice && <p className="form-success">{notice}</p>}
      {saveMutation.isError && <p className="form-error">The item could not be saved.</p>}
      {archiveMutation.isError && <p className="form-error">The item could not be archived.</p>}

      <div className="cms-split">
        <form className="profile-form cms-form" onSubmit={handleSubmit}>
          {section === "categories" && (
            <CategoryFields value={categoryForm} onChange={setCategoryForm} />
          )}
          {section === "tags" && <TagFields value={tagForm} onChange={setTagForm} />}
          {section === "technologies" && (
            <TechnologyFields value={technologyForm} onChange={setTechnologyForm} />
          )}
          {section === "skill-groups" && (
            <SkillGroupFields
              technologies={taxonomyQuery.data?.technologies ?? []}
              value={skillGroupForm}
              onChange={setSkillGroupForm}
            />
          )}
          <div className="form-actions">
            <button type="submit" disabled={isBusy}>
              {isBusy ? "Saving..." : "Save"}
            </button>
            <button className="secondary-button" type="button" onClick={resetForm}>
              New
            </button>
          </div>
        </form>

        <div className="data-region" aria-live="polite">
          {taxonomyQuery.isLoading ? (
            <p className="muted">Loading taxonomy...</p>
          ) : rows.length === 0 ? (
            <p className="muted">No records yet.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Status</th>
                  <th>Order</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <strong>{row.name}</strong>
                      <span>{row.slug}</span>
                    </td>
                    <td>{row.status}</td>
                    <td>{row.displayOrder}</td>
                    <td>
                      <div className="table-actions">
                        <button type="button" onClick={() => editRow(row)}>
                          Edit
                        </button>
                        {row.id !== null && row.status === "ACTIVE" && (
                          <button
                            className="danger-button"
                            type="button"
                            onClick={() => archiveMutation.mutate(row.id as number)}
                          >
                            Archive
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </section>
  );
}

function CategoryFields({
  value,
  onChange
}: {
  value: Category;
  onChange: (value: Category) => void;
}) {
  return (
    <fieldset>
      <legend>Category</legend>
      <TextField label="Name" value={value.name} onChange={(name) => onChange({ ...value, name })} required />
      <TextField label="Slug" value={value.slug} onChange={(slug) => onChange({ ...value, slug })} required />
      <TextArea
        label="Description"
        value={value.description ?? ""}
        onChange={(description) => onChange({ ...value, description })}
      />
      <TaxonomyMeta value={value} onChange={onChange} />
    </fieldset>
  );
}

function TagFields({ value, onChange }: { value: Tag; onChange: (value: Tag) => void }) {
  return (
    <fieldset>
      <legend>Tag</legend>
      <TextField label="Name" value={value.name} onChange={(name) => onChange({ ...value, name })} required />
      <TextField label="Slug" value={value.slug} onChange={(slug) => onChange({ ...value, slug })} required />
      <TaxonomyMeta value={value} onChange={onChange} />
    </fieldset>
  );
}

function TechnologyFields({
  value,
  onChange
}: {
  value: Technology;
  onChange: (value: Technology) => void;
}) {
  return (
    <fieldset>
      <legend>Technology</legend>
      <div className="form-grid">
        <TextField label="Name" value={value.name} onChange={(name) => onChange({ ...value, name })} required />
        <TextField label="Slug" value={value.slug} onChange={(slug) => onChange({ ...value, slug })} required />
        <label>
          Type
          <select
            value={value.type}
            onChange={(event) => onChange({ ...value, type: event.target.value as TechnologyType })}
          >
            {technologyTypes.map((type) => (
              <option key={type}>{type}</option>
            ))}
          </select>
        </label>
        <label>
          Core
          <select
            value={value.core ? "yes" : "no"}
            onChange={(event) => onChange({ ...value, core: event.target.value === "yes" })}
          >
            <option value="yes">Yes</option>
            <option value="no">No</option>
          </select>
        </label>
      </div>
      <TextArea label="Description" value={value.description ?? ""} onChange={(description) => onChange({ ...value, description })} />
      <TextArea label="How I use it" value={value.howIUseIt ?? ""} onChange={(howIUseIt) => onChange({ ...value, howIUseIt })} />
      <TaxonomyMeta value={value} onChange={onChange} />
    </fieldset>
  );
}

function SkillGroupFields({
  value,
  onChange,
  technologies
}: {
  value: SkillGroupPayload;
  onChange: (value: SkillGroupPayload) => void;
  technologies: Technology[];
}) {
  return (
    <fieldset>
      <legend>Skill group</legend>
      <TextField label="Name" value={value.name} onChange={(name) => onChange({ ...value, name })} required />
      <TextField label="Slug" value={value.slug} onChange={(slug) => onChange({ ...value, slug })} required />
      <TextArea label="Description" value={value.description ?? ""} onChange={(description) => onChange({ ...value, description })} />
      <TaxonomyMeta value={value} onChange={onChange} />
      <fieldset className="nested-fieldset">
        <legend>Technologies</legend>
        <div className="checkbox-grid">
          {technologies.filter((technology) => technology.status === "ACTIVE").map((technology) => (
            <label key={technology.id}>
              <input
                checked={technology.id !== null && value.technologyIds.includes(technology.id)}
                type="checkbox"
                onChange={(event) => {
                  if (technology.id === null) return;
                  const nextIds = event.target.checked
                    ? [...value.technologyIds, technology.id]
                    : value.technologyIds.filter((id) => id !== technology.id);
                  onChange({ ...value, technologyIds: nextIds });
                }}
              />
              {technology.name}
            </label>
          ))}
        </div>
      </fieldset>
    </fieldset>
  );
}

function TaxonomyMeta<T extends { status: TaxonomyStatus; displayOrder: number }>({
  value,
  onChange
}: {
  value: T;
  onChange: (value: T) => void;
}) {
  return (
    <div className="form-grid">
      <label>
        Status
        <select value={value.status} onChange={(event) => onChange({ ...value, status: event.target.value as TaxonomyStatus })}>
          {statuses.map((status) => (
            <option key={status}>{status}</option>
          ))}
        </select>
      </label>
      <label>
        Display order
        <input
          type="number"
          value={value.displayOrder}
          onChange={(event) => onChange({ ...value, displayOrder: Number(event.target.value) })}
        />
      </label>
    </div>
  );
}

function TextField({
  label,
  value,
  onChange,
  required = false
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
}) {
  return (
    <label>
      {label}
      <input required={required} value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function TextArea({
  label,
  value,
  onChange
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label>
      {label}
      <textarea rows={4} value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
