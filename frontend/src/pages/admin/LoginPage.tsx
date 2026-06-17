import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import { Navigate, useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../features/auth/AuthProvider";

export function LoginPage() {
  const { login, session } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (session) {
    return <Navigate to="/admin" replace />;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    const formData = new FormData(event.currentTarget);
    const email = String(formData.get("email") ?? "");
    const password = String(formData.get("password") ?? "");

    try {
      await login(email, password);
      const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? "/admin";
      navigate(from, { replace: true });
    } catch {
      setError("Invalid email or password.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <p className="eyebrow">Admin</p>
        <h1>Sign in</h1>
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input type="email" name="email" autoComplete="email" required />
          </label>
          <label>
            Password
            <input type="password" name="password" autoComplete="current-password" required />
          </label>
          {error && (
            <p className="form-error" role="alert">
              {error}
            </p>
          )}
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing in" : "Continue"}
          </button>
        </form>
        <Link to="/">Back to public site</Link>
      </section>
    </main>
  );
}
