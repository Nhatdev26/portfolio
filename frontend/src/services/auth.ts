import { adminApi, publicApi } from "./apiClient";

export type UserRole = "ADMIN" | "EDITOR" | "REVIEWER" | "VIEWER";
export type UserStatus = "ACTIVE" | "DISABLED" | "LOCKED";

export type CurrentUser = {
  id: number;
  email: string;
  role: UserRole;
  status: UserStatus;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: CurrentUser;
};

export type LogoutResponse = {
  revoked: boolean;
};

export function loginAdmin(email: string, password: string) {
  return publicApi.post<AuthResponse>("/auth/login", { email, password });
}

export function getCurrentUser(accessToken: string) {
  return adminApi.get<CurrentUser>("/auth/me", accessToken);
}

export function logoutAdmin(refreshToken: string) {
  return publicApi.post<LogoutResponse>("/auth/logout", { refreshToken });
}
