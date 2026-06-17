import { NavLink, Outlet } from "react-router-dom";

import { useAuth } from "../features/auth/AuthProvider";

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
  const { logout, session } = useAuth();

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
        <div className="admin-account">
          <span>{session?.user.email}</span>
          <button type="button" onClick={() => void logout()}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
