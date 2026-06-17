import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type ChangeEvent, type FormEvent, useEffect, useState } from "react";

import { useAuth } from "../../features/auth/AuthProvider";
import {
  getAdminProfile,
  saveAdminProfile,
  type AdminProfile,
  type ProfileContent,
  type ProfileContentStatus,
  type ProfileLanguage,
  type ProfileStatus,
  type SocialLink,
  type SocialLinkPlatform,
  type SocialLinkStatus
} from "../../services/profile";

const profileStatuses: ProfileStatus[] = ["DRAFT", "ACTIVE", "INACTIVE"];
const contentStatuses: ProfileContentStatus[] = ["DRAFT", "ACTIVE", "INACTIVE"];
const socialStatuses: SocialLinkStatus[] = ["ACTIVE", "INACTIVE"];
const platforms: SocialLinkPlatform[] = ["GITHUB", "LINKEDIN", "EMAIL", "PORTFOLIO", "OTHER"];
const languages: ProfileLanguage[] = ["EN", "VI"];

export function ProfilePage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [form, setForm] = useState<AdminProfile>(() => emptyProfile());
  const [notice, setNotice] = useState<string | null>(null);

  const profileQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-profile"],
    queryFn: () => getAdminProfile(requireToken(session?.accessToken))
  });

  const saveMutation = useMutation({
    mutationFn: (nextProfile: AdminProfile) =>
      saveAdminProfile(requireToken(session?.accessToken), nextProfile),
    onSuccess: (savedProfile) => {
      const normalized = normalizeProfile(savedProfile);
      setForm(normalized);
      setNotice("Profile saved.");
      queryClient.setQueryData(["admin-profile"], normalized);
    }
  });

  useEffect(() => {
    if (profileQuery.data) {
      setForm(normalizeProfile(profileQuery.data));
    }
  }, [profileQuery.data]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice(null);
    saveMutation.mutate(form);
  }

  function updateProfileField<K extends keyof AdminProfile>(field: K, value: AdminProfile[K]) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function updateContent(index: number, updates: Partial<ProfileContent>) {
    setForm((current) => ({
      ...current,
      contents: current.contents.map((content, contentIndex) =>
        contentIndex === index ? { ...content, ...updates } : content
      )
    }));
  }

  function addSocialLink() {
    setForm((current) => ({
      ...current,
      socialLinks: [
        ...current.socialLinks,
        {
          id: null,
          platform: "PORTFOLIO",
          label: "",
          url: "",
          displayOrder: current.socialLinks.length + 1,
          status: "ACTIVE"
        }
      ]
    }));
  }

  function updateSocialLink(index: number, updates: Partial<SocialLink>) {
    setForm((current) => ({
      ...current,
      socialLinks: current.socialLinks.map((link, linkIndex) =>
        linkIndex === index ? { ...link, ...updates } : link
      )
    }));
  }

  function removeSocialLink(index: number) {
    setForm((current) => ({
      ...current,
      socialLinks: current.socialLinks.filter((_, linkIndex) => linkIndex !== index)
    }));
  }

  const isSaving = saveMutation.isPending;

  return (
    <section className="content-panel profile-admin">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Profile CMS</p>
          <h1>Public profile</h1>
        </div>
        {profileQuery.isFetching && <span className="status-chip">Syncing</span>}
      </div>

      {profileQuery.isError && (
        <p className="form-error" role="alert">
          Profile could not be loaded.
        </p>
      )}

      <form className="profile-form" onSubmit={handleSubmit}>
        <fieldset>
          <legend>Identity</legend>
          <div className="form-grid">
            <label>
              Display name
              <input
                required
                maxLength={160}
                value={form.displayName}
                onChange={(event) => updateProfileField("displayName", event.target.value)}
              />
            </label>
            <label>
              Email
              <input
                required
                type="email"
                maxLength={320}
                value={form.email}
                onChange={(event) => updateProfileField("email", event.target.value)}
              />
            </label>
            <label>
              Primary role
              <input
                required
                maxLength={160}
                value={form.primaryRole}
                onChange={(event) => updateProfileField("primaryRole", event.target.value)}
              />
            </label>
            <label>
              Location
              <input
                maxLength={160}
                value={form.location}
                onChange={(event) => updateProfileField("location", event.target.value)}
              />
            </label>
            <label>
              Career direction
              <input
                maxLength={255}
                value={form.careerDirection}
                onChange={(event) => updateProfileField("careerDirection", event.target.value)}
              />
            </label>
            <label>
              Main tech focus
              <input
                maxLength={255}
                value={form.mainTechFocus}
                onChange={(event) => updateProfileField("mainTechFocus", event.target.value)}
              />
            </label>
            <label>
              Profile status
              <select
                value={form.status}
                onChange={(event) =>
                  updateProfileField("status", event.target.value as ProfileStatus)
                }
              >
                {profileStatuses.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </fieldset>

        <fieldset>
          <legend>Localized content</legend>
          <div className="content-language-grid">
            {form.contents.map((content, index) => (
              <article className="language-editor" key={content.language}>
                <div className="language-editor-header">
                  <strong>{content.language}</strong>
                  <select
                    aria-label={`${content.language} content status`}
                    value={content.status}
                    onChange={(event) =>
                      updateContent(index, { status: event.target.value as ProfileContentStatus })
                    }
                  >
                    {contentStatuses.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </div>
                <label>
                  Headline
                  <input
                    required
                    maxLength={255}
                    value={content.headline}
                    onChange={(event) => updateContent(index, { headline: event.target.value })}
                  />
                </label>
                <label>
                  Subheadline
                  <input
                    maxLength={255}
                    value={content.subheadline}
                    onChange={(event) => updateContent(index, { subheadline: event.target.value })}
                  />
                </label>
                <label>
                  Short bio
                  <textarea
                    rows={4}
                    value={content.shortBio}
                    onChange={(event) => updateContent(index, { shortBio: event.target.value })}
                  />
                </label>
                <label>
                  Long bio
                  <textarea
                    rows={7}
                    value={content.longBio}
                    onChange={(event) => updateContent(index, { longBio: event.target.value })}
                  />
                </label>
                <label>
                  SEO title
                  <input
                    maxLength={255}
                    value={content.seoTitle}
                    onChange={(event) => updateContent(index, { seoTitle: event.target.value })}
                  />
                </label>
                <label>
                  SEO description
                  <textarea
                    rows={3}
                    maxLength={320}
                    value={content.seoDescription}
                    onChange={(event) =>
                      updateContent(index, { seoDescription: event.target.value })
                    }
                  />
                </label>
              </article>
            ))}
          </div>
        </fieldset>

        <fieldset>
          <div className="fieldset-heading">
            <legend>Social links</legend>
            <button className="secondary-button" type="button" onClick={addSocialLink}>
              Add link
            </button>
          </div>
          <div className="social-link-list">
            {form.socialLinks.length === 0 && <p className="muted">No social links yet.</p>}
            {form.socialLinks.map((link, index) => (
              <article className="social-link-editor" key={`${link.id ?? "new"}-${index}`}>
                <label>
                  Platform
                  <select
                    value={link.platform}
                    onChange={(event) =>
                      updateSocialLink(index, {
                        platform: event.target.value as SocialLinkPlatform
                      })
                    }
                  >
                    {platforms.map((platform) => (
                      <option key={platform} value={platform}>
                        {platform}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Label
                  <input
                    required
                    maxLength={120}
                    value={link.label}
                    onChange={(event) => updateSocialLink(index, { label: event.target.value })}
                  />
                </label>
                <label className="wide-field">
                  URL
                  <input
                    required
                    maxLength={2048}
                    value={link.url}
                    onChange={(event) => updateSocialLink(index, { url: event.target.value })}
                  />
                </label>
                <label>
                  Order
                  <input
                    type="number"
                    min={0}
                    value={link.displayOrder}
                    onChange={(event: ChangeEvent<HTMLInputElement>) =>
                      updateSocialLink(index, {
                        displayOrder: Number.parseInt(event.target.value, 10) || 0
                      })
                    }
                  />
                </label>
                <label>
                  Status
                  <select
                    value={link.status}
                    onChange={(event) =>
                      updateSocialLink(index, { status: event.target.value as SocialLinkStatus })
                    }
                  >
                    {socialStatuses.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </label>
                <button
                  className="danger-button"
                  type="button"
                  onClick={() => removeSocialLink(index)}
                >
                  Remove
                </button>
              </article>
            ))}
          </div>
        </fieldset>

        {saveMutation.isError && (
          <p className="form-error" role="alert">
            Profile could not be saved. Check required fields and social URLs.
          </p>
        )}
        {notice && <p className="form-success">{notice}</p>}

        <div className="form-actions">
          <button type="submit" disabled={isSaving || profileQuery.isLoading}>
            {isSaving ? "Saving" : "Save profile"}
          </button>
        </div>
      </form>
    </section>
  );
}

function requireToken(token: string | undefined) {
  if (!token) {
    throw new Error("Admin session is missing.");
  }
  return token;
}

function normalizeProfile(profile: AdminProfile): AdminProfile {
  return {
    ...profile,
    contents: languages.map((language) => {
      const existing = profile.contents.find((content) => content.language === language);
      return existing ?? emptyContent(language);
    }),
    socialLinks: profile.socialLinks.map((link, index) => ({
      ...link,
      displayOrder: link.displayOrder ?? index + 1
    }))
  };
}

function emptyProfile(): AdminProfile {
  return {
    id: null,
    displayName: "",
    email: "",
    location: "",
    primaryRole: "",
    careerDirection: "",
    mainTechFocus: "",
    status: "DRAFT",
    contents: languages.map(emptyContent),
    socialLinks: []
  };
}

function emptyContent(language: ProfileLanguage): ProfileContent {
  return {
    id: null,
    language,
    headline: "",
    subheadline: "",
    shortBio: "",
    longBio: "",
    seoTitle: "",
    seoDescription: "",
    status: language === "EN" ? "ACTIVE" : "DRAFT"
  };
}
