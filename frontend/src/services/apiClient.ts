const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

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
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  let body: BodyInit | undefined;
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(options.body);
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

  if (!response.ok) {
    throw new ApiClientError("API request failed", response.status, payload);
  }

  return payload as T;
}

export const publicApi = {
  get: <T>(path: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "GET" })
};

export const adminApi = {
  get: <T>(path: string, token: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: "GET", token })
};
