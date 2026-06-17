import { adminApi } from "./apiClient";

export type AuditResult = "SUCCESS" | "FAILURE";

export type AuditLog = {
  id: number;
  actorId: number | null;
  actorEmail: string | null;
  action: string;
  entityType: string;
  entityId: string | null;
  entityTitle: string | null;
  result: AuditResult;
  oldValue: unknown;
  newValue: unknown;
  createdAt: string;
};

export type AuditFilters = {
  action?: string;
  entityType?: string;
  actor?: string;
  from?: string;
  to?: string;
};

export function listAuditLogs(accessToken: string, filters: AuditFilters) {
  const params = new URLSearchParams();
  addParam(params, "action", filters.action);
  addParam(params, "entityType", filters.entityType);
  addParam(params, "actor", filters.actor);
  addDateParam(params, "from", filters.from);
  addDateParam(params, "to", filters.to);
  const query = params.toString();
  return adminApi.get<AuditLog[]>(`/api/admin/audit-logs${query ? `?${query}` : ""}`, accessToken);
}

function addParam(params: URLSearchParams, key: string, value: string | undefined) {
  if (value && value.trim()) {
    params.set(key, value.trim());
  }
}

function addDateParam(params: URLSearchParams, key: string, value: string | undefined) {
  if (value && value.trim()) {
    params.set(key, new Date(value).toISOString());
  }
}
