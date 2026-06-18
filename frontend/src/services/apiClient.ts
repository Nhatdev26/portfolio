export const API_BASE_URL =
  window.__PORTFOLIO_CONFIG__?.API_BASE_URL ?? import.meta.env.VITE_API_BASE_URL ?? "";

const AUTH_STORAGE_KEY = "portfolio-cms-auth";
const AUTH_UPDATED_EVENT = "portfolio-cms-auth-updated";

export class ApiClientError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly payload: unknown
  ) {
    super(message);
    this.name = "ApiClientError";
  }
}

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  token?: string;
  skipAuthRefresh?: boolean;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  let body: BodyInit | undefined;
  if (options.body !== undefined) {
    if (options.body instanceof FormData) {
      body = options.body;
    } else {
      headers.set("Content-Type", "application/json");
      body = JSON.stringify(options.body);
    }
  }

  if (options.token) {
    headers.set("Authorization", `Bearer ${options.token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    body,
    headers
  });

  const contentType = response.headers.get("content-type");
  const payload = contentType?.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok && response.status === 401 && options.token && !options.skipAuthRefresh) {
    const refreshedToken = await refreshStoredSession();
    if (refreshedToken) {
      return request<T>(path, { ...options, skipAuthRefresh: true, token: refreshedToken });
    }
  }

  if (!response.ok) {
    throw new ApiClientError("API request failed", response.status, payload);
  }

  return payload as T;
}

type StoredAuthSession = {
  accessToken: string;
  refreshToken: string;
  user: unknown;
  expiresAt?: number;
};

type AuthRefreshResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: unknown;
};

let refreshPromise: Promise<string | null> | null = null;

async function refreshStoredSession() {
  const currentSession = readStoredSession();
  if (!currentSession?.refreshToken) {
    clearStoredSession();
    return null;
  }

  if (!refreshPromise) {
    refreshPromise = fetch(`${API_BASE_URL}/auth/refresh`, {
      body: JSON.stringify({ refreshToken: currentSession.refreshToken }),
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json"
      },
      method: "POST"
    })
      .then(async (response) => {
        if (!response.ok) {
          clearStoredSession(true);
          return null;
        }

        const payload = (await response.json()) as AuthRefreshResponse;
        const nextSession = {
          accessToken: payload.accessToken,
          expiresAt: Date.now() + payload.expiresIn * 1000,
          refreshToken: payload.refreshToken,
          user: payload.user
        };
        writeStoredSession(nextSession);
        return nextSession.accessToken;
      })
      .catch(() => {
        clearStoredSession(true);
        return null;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

function readStoredSession(): StoredAuthSession | null {
  try {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as StoredAuthSession) : null;
  } catch {
    clearStoredSession();
    return null;
  }
}

function writeStoredSession(session: StoredAuthSession) {
  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
  window.dispatchEvent(new CustomEvent(AUTH_UPDATED_EVENT));
}

function clearStoredSession(redirectToLogin = false) {
  window.localStorage.removeItem(AUTH_STORAGE_KEY);
  window.dispatchEvent(new CustomEvent(AUTH_UPDATED_EVENT));

  if (
    redirectToLogin &&
    window.location.pathname.startsWith("/admin") &&
    window.location.pathname !== "/admin/login"
  ) {
    window.location.assign("/admin/login");
  }
}

export const publicApi = {
  get: <T>(path: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "GET" }),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, body, method: "POST" })
};

export const adminApi = {
  get: <T>(path: string, token: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "GET", token }),
  put: <T>(path: string, token: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, body, method: "PUT", token }),
  post: <T>(path: string, token: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, body, method: "POST", token }),
  patch: <T>(path: string, token: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, body, method: "PATCH", token }),
  delete: <T>(path: string, token: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "DELETE", token })
};
