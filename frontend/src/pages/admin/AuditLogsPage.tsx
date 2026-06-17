import { useQuery } from "@tanstack/react-query";
import { type FormEvent, useMemo, useState } from "react";

import { useAuth } from "../../features/auth/AuthProvider";
import { listAuditLogs, type AuditFilters, type AuditLog } from "../../services/audit";

const actions = [
  "LOGIN_SUCCESS",
  "LOGIN_FAILURE",
  "LOGOUT",
  "PROFILE_UPDATE",
  "PROJECT_CREATE",
  "PROJECT_UPDATE",
  "PROJECT_PUBLISH",
  "PROJECT_ARCHIVE",
  "PROJECT_DELETE",
  "NOTE_CREATE",
  "NOTE_UPDATE",
  "NOTE_PUBLISH",
  "NOTE_ARCHIVE",
  "NOTE_DELETE",
  "CV_UPLOAD",
  "CV_ACTIVATE",
  "CV_ARCHIVE",
  "CV_DELETE"
];

const entityTypes = [
  "AUTH",
  "PROFILE",
  "PROJECT",
  "TECHNICAL_NOTE",
  "CATEGORY",
  "TAG",
  "TECHNOLOGY",
  "SKILL_GROUP",
  "CV_FILE"
];

export function AuditLogsPage() {
  const { session } = useAuth();
  const [filters, setFilters] = useState<AuditFilters>({});
  const [draftFilters, setDraftFilters] = useState<AuditFilters>({});
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const auditQuery = useQuery({
    enabled: Boolean(session?.accessToken),
    queryKey: ["audit-logs", filters],
    queryFn: () => listAuditLogs(requireToken(session?.accessToken), filters)
  });

  const logs = auditQuery.data ?? [];
  const selectedLog = useMemo(
    () => logs.find((log) => log.id === selectedId) ?? logs[0] ?? null,
    [logs, selectedId]
  );
  const successful = logs.filter((log) => log.result === "SUCCESS").length;
  const failed = logs.filter((log) => log.result === "FAILURE").length;
  const actorCount = new Set(logs.map((log) => log.actorEmail).filter(Boolean)).size;

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSelectedId(null);
    setFilters(draftFilters);
  }

  function clearFilters() {
    setDraftFilters({});
    setFilters({});
    setSelectedId(null);
  }

  return (
    <section className="content-panel audit-page">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Audit Trail</p>
          <h1>Audit logs</h1>
          <p className="muted">Read-only activity history for important CMS changes.</p>
        </div>
        {auditQuery.isFetching && <span className="status-chip">Syncing</span>}
      </div>

      <div className="audit-metrics" aria-label="Audit summary">
        <article>
          <span>Total events</span>
          <strong>{logs.length}</strong>
        </article>
        <article>
          <span>Successful</span>
          <strong>{successful}</strong>
        </article>
        <article>
          <span>Failed</span>
          <strong>{failed}</strong>
        </article>
        <article>
          <span>Actors</span>
          <strong>{actorCount}</strong>
        </article>
      </div>

      <form className="audit-filter-bar" onSubmit={submit}>
        <label>
          Action
          <select value={draftFilters.action ?? ""} onChange={(event) => setDraftFilters({ ...draftFilters, action: event.target.value })}>
            <option value="">All actions</option>
            {actions.map((action) => (
              <option key={action} value={action}>{labelize(action)}</option>
            ))}
          </select>
        </label>
        <label>
          Entity
          <select value={draftFilters.entityType ?? ""} onChange={(event) => setDraftFilters({ ...draftFilters, entityType: event.target.value })}>
            <option value="">All entities</option>
            {entityTypes.map((entityType) => (
              <option key={entityType} value={entityType}>{labelize(entityType)}</option>
            ))}
          </select>
        </label>
        <label>
          Actor
          <input
            placeholder="admin@example.com"
            value={draftFilters.actor ?? ""}
            onChange={(event) => setDraftFilters({ ...draftFilters, actor: event.target.value })}
          />
        </label>
        <label>
          From
          <input
            type="datetime-local"
            value={draftFilters.from ?? ""}
            onChange={(event) => setDraftFilters({ ...draftFilters, from: event.target.value })}
          />
        </label>
        <label>
          To
          <input
            type="datetime-local"
            value={draftFilters.to ?? ""}
            onChange={(event) => setDraftFilters({ ...draftFilters, to: event.target.value })}
          />
        </label>
        <div className="audit-filter-actions">
          <button type="submit">Apply</button>
          <button className="secondary-button" type="button" onClick={clearFilters}>Reset</button>
        </div>
      </form>

      {auditQuery.isError && <p className="form-error">Audit logs could not be loaded.</p>}

      <div className="audit-workspace">
        <AuditTable
          logs={logs}
          isLoading={auditQuery.isLoading}
          selectedId={selectedLog?.id ?? null}
          onSelect={setSelectedId}
        />
        <AuditDetail log={selectedLog} />
      </div>
    </section>
  );
}

function AuditTable({
  logs,
  isLoading,
  selectedId,
  onSelect
}: {
  logs: AuditLog[];
  isLoading: boolean;
  selectedId: number | null;
  onSelect: (id: number) => void;
}) {
  if (isLoading) {
    return <p className="muted">Loading audit logs...</p>;
  }
  if (logs.length === 0) {
    return <p className="muted">No audit logs match these filters.</p>;
  }
  return (
    <div className="data-region audit-table-region" aria-live="polite">
      <table className="data-table audit-table">
        <thead>
          <tr>
            <th>Time</th>
            <th>Actor</th>
            <th>Action</th>
            <th>Entity</th>
            <th>Result</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr
              className={log.id === selectedId ? "selected-row" : undefined}
              key={log.id}
              onClick={() => onSelect(log.id)}
            >
              <td>
                <strong>{formatDate(log.createdAt)}</strong>
                <span>{formatTime(log.createdAt)}</span>
              </td>
              <td>
                {log.actorEmail ?? "System"}
                {log.actorId && <span>ID {log.actorId}</span>}
              </td>
              <td>{labelize(log.action)}</td>
              <td>
                {labelize(log.entityType)}
                <span>{log.entityTitle ?? log.entityId ?? "No title"}</span>
              </td>
              <td>
                <span className={log.result === "SUCCESS" ? "status-chip" : "status-chip danger"}>
                  {log.result}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function AuditDetail({ log }: { log: AuditLog | null }) {
  if (!log) {
    return (
      <aside className="audit-detail-panel">
        <p className="eyebrow">Detail</p>
        <p className="muted">Select an audit row to inspect safe old and new values.</p>
      </aside>
    );
  }
  return (
    <aside className="audit-detail-panel">
      <div>
        <p className="eyebrow">Detail</p>
        <h2>{labelize(log.action)}</h2>
        <p className="muted">{formatDateTime(log.createdAt)}</p>
      </div>
      <dl className="audit-detail-list">
        <div>
          <dt>Actor</dt>
          <dd>{log.actorEmail ?? "System"}</dd>
        </div>
        <div>
          <dt>Entity</dt>
          <dd>{labelize(log.entityType)} {log.entityId ? `#${log.entityId}` : ""}</dd>
        </div>
        <div>
          <dt>Title</dt>
          <dd>{log.entityTitle ?? "No title"}</dd>
        </div>
      </dl>
      <JsonBlock title="Old value" value={log.oldValue} />
      <JsonBlock title="New value" value={log.newValue} />
    </aside>
  );
}

function JsonBlock({ title, value }: { title: string; value: unknown }) {
  return (
    <section className="json-block">
      <h3>{title}</h3>
      <pre>{value == null ? "No value" : JSON.stringify(value, null, 2)}</pre>
    </section>
  );
}

function labelize(value: string) {
  return value.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en", { dateStyle: "medium" }).format(new Date(value));
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat("en", { timeStyle: "short" }).format(new Date(value));
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

function requireToken(token: string | undefined) {
  if (!token) throw new Error("Missing admin session.");
  return token;
}
