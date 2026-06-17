import { NavLink, Outlet } from "react-router-dom";

const links = [
  { to: "/", label: "Home" },
  { to: "/about", label: "About" },
  { to: "/projects", label: "Projects" },
  { to: "/notes", label: "Notes" },
  { to: "/cv", label: "CV" }
];

export function PublicLayout() {
  return (
    <div className="page-shell">
      <header className="site-header">
        <NavLink className="brand" to="/">
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
    </div>
  );
}

