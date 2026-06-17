import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";

import { useAuth } from "../../features/auth/AuthProvider";
import {
  activateCvFile,
  archiveCvFile,
  listAdminCvFiles,
  uploadCvFile,
  type CvFile
} from "../../services/cv";
import type { ContentLanguage } from "../../services/content";

const languages: ContentLanguage[] = ["EN", "VI"];

export function CvFilesPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [language, setLanguage] = useState<ContentLanguage>("EN");
  const [targetRole, setTargetRole] = useState("backend-developer");
  const [version, setVersion] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const cvFilesQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["admin-cv-files"],
    queryFn: () => listAdminCvFiles(requireToken(session?.accessToken))
  });

  const uploadMutation = useMutation({
    mutationFn: () => {
      if (!file) throw new Error("PDF file is required.");
      return uploadCvFile(requireToken(session?.accessToken), {
        file,
        language,
        targetRole,
        version
      });
    },
    onSuccess: async () => {
      setNotice("CV uploaded as draft.");
      setVersion("");
      setFile(null);
      await queryClient.invalidateQueries({ queryKey: ["admin-cv-files"] });
    }
  });

  const activateMutation = useMutation({
    mutationFn: (id: number) => activateCvFile(requireToken(session?.accessToken), id),
    onSuccess: async () => {
      setNotice("CV activated.");
      await queryClient.invalidateQueries({ queryKey: ["admin-cv-files"] });
    }
  });

  const archiveMutation = useMutation({
    mutationFn: (id: number) => archiveCvFile(requireToken(session?.accessToken), id),
    onSuccess: async () => {
      setNotice("CV archived.");
      await queryClient.invalidateQueries({ queryKey: ["admin-cv-files"] });
    }
  });

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice(null);
    uploadMutation.mutate();
  }

  const isBusy = uploadMutation.isPending || activateMutation.isPending || archiveMutation.isPending;

  return (
    <section className="content-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">CV Files CMS</p>
          <h1>CV files</h1>
        </div>
        {cvFilesQuery.isFetching && <span className="status-chip">Syncing</span>}
      </div>

      {notice && <p className="form-success">{notice}</p>}
      {cvFilesQuery.isError && <p className="form-error">CV files could not be loaded.</p>}
      {uploadMutation.isError && <p className="form-error">Upload failed. Use a PDF file up to 5 MB.</p>}
      {activateMutation.isError && <p className="form-error">CV could not be activated.</p>}
      {archiveMutation.isError && <p className="form-error">CV could not be archived.</p>}

      <div className="cms-split">
        <form className="profile-form cms-form" onSubmit={submit}>
          <fieldset>
            <legend>Upload PDF</legend>
            <div className="form-grid">
              <label>
                Language
                <select value={language} onChange={(event) => setLanguage(event.target.value as ContentLanguage)}>
                  {languages.map((nextLanguage) => (
                    <option key={nextLanguage}>{nextLanguage}</option>
                  ))}
                </select>
              </label>
              <label>
                Target role
                <input
                  required
                  value={targetRole}
                  onChange={(event) => setTargetRole(event.target.value)}
                />
              </label>
              <label>
                Version
                <input
                  required
                  placeholder="2026.01"
                  value={version}
                  onChange={(event) => setVersion(event.target.value)}
                />
              </label>
              <label>
                PDF file
                <input
                  required
                  accept="application/pdf,.pdf"
                  type="file"
                  onChange={(event) => setFile(event.target.files?.[0] ?? null)}
                />
              </label>
            </div>
            <p className="muted">Uploads are saved as draft. Activate the selected file after verifying it.</p>
          </fieldset>
          <div className="form-actions">
            <button disabled={isBusy} type="submit">
              {uploadMutation.isPending ? "Uploading..." : "Upload CV"}
            </button>
          </div>
        </form>

        <CvFilesTable
          files={cvFilesQuery.data ?? []}
          isLoading={cvFilesQuery.isLoading}
          isBusy={isBusy}
          onActivate={(id) => activateMutation.mutate(id)}
          onArchive={(id) => archiveMutation.mutate(id)}
        />
      </div>
    </section>
  );
}

function CvFilesTable({
  files,
  isLoading,
  isBusy,
  onActivate,
  onArchive
}: {
  files: CvFile[];
  isLoading: boolean;
  isBusy: boolean;
  onActivate: (id: number) => void;
  onArchive: (id: number) => void;
}) {
  if (isLoading) {
    return <p className="muted">Loading CV files...</p>;
  }
  if (files.length === 0) {
    return <p className="muted">No CV files yet.</p>;
  }
  return (
    <div className="data-region" aria-live="polite">
      <table className="data-table">
        <thead>
          <tr>
            <th>File</th>
            <th>Target</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {files.map((cvFile) => (
            <tr className={cvFile.status === "ACTIVE" ? "active-row" : undefined} key={cvFile.id}>
              <td>
                <strong>{cvFile.originalFilename}</strong>
                <span>{formatBytes(cvFile.fileSize)} · version {cvFile.version}</span>
              </td>
              <td>
                {cvFile.language}
                <span>{cvFile.targetRole}</span>
              </td>
              <td>
                <span className={cvFile.status === "ACTIVE" ? "status-chip" : "status-chip quiet"}>
                  {cvFile.status}
                </span>
              </td>
              <td>
                <div className="table-actions">
                  {cvFile.status !== "ACTIVE" && (
                    <button disabled={isBusy} type="button" onClick={() => onActivate(cvFile.id)}>
                      Activate
                    </button>
                  )}
                  {cvFile.status !== "ARCHIVED" && (
                    <button disabled={isBusy} type="button" onClick={() => onArchive(cvFile.id)}>
                      Archive
                    </button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatBytes(bytes: number) {
  return new Intl.NumberFormat("en", {
    maximumFractionDigits: 1,
    style: "unit",
    unit: "megabyte"
  }).format(bytes / 1024 / 1024);
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
