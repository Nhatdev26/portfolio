import { NavLink, Outlet } from "react-router-dom";

const adminLinks = [
  { to: "/admin", label: "Dashboard" },
  { to: "/admin/profile", label: "Profile" },
  { to: "/admin/projects", label: "Projects" },
  { to: "/admin/notes", label: "Notes" },
  { to: "/admin/technologies", label: "Technologies" },
  { to: "/admin/media", label: "Media" },
  { to: "/admin/audit-logs", label: "Audit" }
];

export function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <NavLink className="brand" to="/admin">
          CMS Admin
        </NavLink>
        <nav className="admin-nav" aria-label="Admin navigation">
          {adminLinks.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.to === "/admin"}>
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}

