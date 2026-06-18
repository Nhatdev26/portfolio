import { Link } from "react-router-dom";

import { Seo } from "../components/common/Seo";

export function NotFoundPage() {
  return (
    <main className="center-page">
      <Seo title="Page not found" description="The requested portfolio page could not be found." noIndex />
      <section className="content-panel compact">
        <p className="eyebrow">404</p>
        <h1>Page not found</h1>
        <Link to="/">Return home</Link>
      </section>
    </main>
  );
}
