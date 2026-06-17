import { NavLink, Outlet } from "react-router-dom";

const links = [
  { to: "/about", label: "About" },
  { to: "/projects", label: "Projects" },
  { to: "/skills", label: "Skills" },
  { to: "/notes", label: "Blog" }
];

export function PublicLayout() {
  return (
    <div className="page-shell public-shell">
      <header className="site-header">
        <NavLink className="brand" to="/">
          <span className="brand-mark" aria-hidden="true">PC</span>
          Portfolio CMS
        </NavLink>
        <nav className="nav-links" aria-label="Public navigation">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to}>
              {link.label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
      <nav className="mobile-bottom-nav" aria-label="Mobile public navigation">
        {links.map((link) => (
          <NavLink key={link.to} to={link.to}>
            {link.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
