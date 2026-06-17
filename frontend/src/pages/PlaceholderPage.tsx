type PlaceholderPageProps = {
  title: string;
};

export function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <section className="content-panel">
      <p className="eyebrow">Portfolio CMS</p>
      <h1>{title}</h1>
      <p className="muted">This route is reserved for its upcoming vertical slice.</p>
    </section>
  );
}

