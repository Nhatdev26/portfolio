import { publicApi } from "./apiClient";

export type BackendHealth = {
  status: string;
  service: string;
};

export function getBackendHealth() {
  return publicApi.get<BackendHealth>("/api/health");
}

