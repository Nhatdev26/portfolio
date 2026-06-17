import {
  createContext,
  type PropsWithChildren,
  useContext,
  useMemo,
  useState
} from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";

import { loginAdmin, logoutAdmin, type CurrentUser } from "../../services/auth";

const STORAGE_KEY = "portfolio-cms-auth";

type AuthSession = {
  accessToken: string;
  refreshToken: string;
  user: CurrentUser;
};

type AuthContextValue = {
  session: AuthSession | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<AuthSession | null>(() => readSession());

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      login: async (email, password) => {
        const response = await loginAdmin(email, password);
        const nextSession = {
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          user: response.user
        };
        writeSession(nextSession);
        setSession(nextSession);
      },
      logout: async () => {
        const refreshToken = session?.refreshToken;
        clearSession();
        setSession(null);

        if (refreshToken) {
          await logoutAdmin(refreshToken).catch(() => undefined);
        }
      }
    }),
    [session]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function RequireAuth() {
  const { session } = useAuth();
  const location = useLocation();

  if (!session) {
    return <Navigate to="/admin/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("useAuth must be used inside AuthProvider.");
  }
  return value;
}

function readSession(): AuthSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AuthSession) : null;
  } catch {
    clearSession();
    return null;
  }
}

function writeSession(session: AuthSession) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

function clearSession() {
  localStorage.removeItem(STORAGE_KEY);
}
