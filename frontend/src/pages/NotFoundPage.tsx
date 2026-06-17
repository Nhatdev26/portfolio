import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <main className="center-page">
      <section className="content-panel compact">
        <p className="eyebrow">404</p>
        <h1>Page not found</h1>
        <Link to="/">Return home</Link>
      </section>
    </main>
  );
}

