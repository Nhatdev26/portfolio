import { useAuth } from "../../features/auth/AuthProvider";

export function AdminDashboardPage() {
  const { session } = useAuth();

  return (
    <section className="content-panel">
      <p className="eyebrow">Admin Dashboard</p>
      <h1>CMS workspace</h1>
      <p className="muted">Signed in as {session?.user.email}</p>
      <div className="metric-grid">
        <article>
          <span>Profile</span>
          <strong>Pending</strong>
        </article>
        <article>
          <span>Projects</span>
          <strong>Pending</strong>
        </article>
        <article>
          <span>Notes</span>
          <strong>Pending</strong>
        </article>
      </div>
    </section>
  );
}
