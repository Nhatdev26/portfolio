import { Link } from "react-router-dom";

export function LoginPage() {
  return (
    <main className="auth-page">
      <section className="auth-panel">
        <p className="eyebrow">Admin</p>
        <h1>Sign in</h1>
        <form className="auth-form">
          <label>
            Email
            <input type="email" name="email" autoComplete="email" />
          </label>
          <label>
            Password
            <input type="password" name="password" autoComplete="current-password" />
          </label>
          <button type="button">Continue</button>
        </form>
        <Link to="/">Back to public site</Link>
      </section>
    </main>
  );
}

