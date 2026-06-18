import {
  createContext,
  type PropsWithChildren,
  useContext,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState
} from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";

import {
  getCurrentUser,
  loginAdmin,
  logoutAdmin,
  refreshAdmin,
  type AuthResponse,
  type CurrentUser
} from "../../services/auth";

const STORAGE_KEY = "portfolio-cms-auth";
const AUTH_UPDATED_EVENT = "portfolio-cms-auth-updated";
const REFRESH_BUFFER_MS = 60_000;

type AuthSession = {
  accessToken: string;
  refreshToken: string;
  user: CurrentUser;
  expiresAt?: number;
};

type AuthContextValue = {
  session: AuthSession | null;
  isChecking: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<AuthSession | null>(() => readSession());
  const [isChecking, setIsChecking] = useState(() => Boolean(readSession()));
  const refreshPromiseRef = useRef<Promise<AuthSession | null> | null>(null);

  const persistSession = useCallback((nextSession: AuthSession | null) => {
    if (nextSession) {
      writeSession(nextSession);
    } else {
      clearSession();
    }
    setSession(nextSession);
  }, []);

  const refreshSession = useCallback(async () => {
    const current = readSession();
    if (!current?.refreshToken) {
      persistSession(null);
      return null;
    }

    if (!refreshPromiseRef.current) {
      refreshPromiseRef.current = refreshAdmin(current.refreshToken)
        .then((response) => {
          const nextSession = sessionFromAuthResponse(response);
          persistSession(nextSession);
          return nextSession;
        })
        .catch(() => {
          persistSession(null);
          return null;
        })
        .finally(() => {
          refreshPromiseRef.current = null;
        });
    }

    return refreshPromiseRef.current;
  }, [persistSession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      isChecking,
      session,
      login: async (email, password) => {
        const response = await loginAdmin(email, password);
        persistSession(sessionFromAuthResponse(response));
      },
      logout: async () => {
        const refreshToken = session?.refreshToken;
        persistSession(null);

        if (refreshToken) {
          await logoutAdmin(refreshToken).catch(() => undefined);
        }
      }
    }),
    [isChecking, persistSession, session]
  );

  useEffect(() => {
    let cancelled = false;
    const current = readSession();

    if (!current) {
      setIsChecking(false);
      return;
    }
    const storedSession = current;

    async function bootstrapSession() {
      setIsChecking(true);
      try {
        const shouldRefresh =
          !storedSession.expiresAt || storedSession.expiresAt - Date.now() <= REFRESH_BUFFER_MS;

        if (shouldRefresh) {
          await refreshSession();
          return;
        }

        const user = await getCurrentUser(storedSession.accessToken);
        if (!cancelled) {
          persistSession({ ...storedSession, user });
        }
      } catch {
        await refreshSession();
      } finally {
        if (!cancelled) {
          setIsChecking(false);
        }
      }
    }

    void bootstrapSession();

    return () => {
      cancelled = true;
    };
  }, [persistSession, refreshSession]);

  useEffect(() => {
    if (!session?.expiresAt) return;

    const delay = Math.max(session.expiresAt - Date.now() - REFRESH_BUFFER_MS, 0);
    const timeout = window.setTimeout(() => {
      void refreshSession();
    }, delay);

    return () => window.clearTimeout(timeout);
  }, [refreshSession, session?.expiresAt]);

  useEffect(() => {
    const syncSession = () => {
      setSession(readSession());
    };

    window.addEventListener(AUTH_UPDATED_EVENT, syncSession);
    window.addEventListener("storage", syncSession);

    return () => {
      window.removeEventListener(AUTH_UPDATED_EVENT, syncSession);
      window.removeEventListener("storage", syncSession);
    };
  }, []);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function RequireAuth() {
  const { isChecking, session } = useAuth();
  const location = useLocation();

  if (isChecking) {
    return <main className="auth-page"><p className="muted">Checking session...</p></main>;
  }

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

function sessionFromAuthResponse(response: AuthResponse): AuthSession {
  return {
    accessToken: response.accessToken,
    refreshToken: response.refreshToken,
    user: response.user,
    expiresAt: Date.now() + response.expiresIn * 1000
  };
}
