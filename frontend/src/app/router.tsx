import { createBrowserRouter } from "react-router-dom";

import { RequireAuth } from "../features/auth/AuthProvider";
import { AdminLayout } from "../layouts/AdminLayout";
import { PublicLayout } from "../layouts/PublicLayout";
import { AdminDashboardPage } from "../pages/admin/AdminDashboardPage";
import { LoginPage } from "../pages/admin/LoginPage";
import { ProfilePage } from "../pages/admin/ProfilePage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { PlaceholderPage } from "../pages/PlaceholderPage";
import { AboutPage } from "../pages/public/AboutPage";
import { HomePage } from "../pages/public/HomePage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <PublicLayout />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "about", element: <AboutPage /> },
      { path: "projects", element: <PlaceholderPage title="Projects" /> },
      { path: "projects/:slug", element: <PlaceholderPage title="Project detail" /> },
      { path: "notes", element: <PlaceholderPage title="Technical notes" /> },
      { path: "notes/:slug", element: <PlaceholderPage title="Technical note detail" /> },
      { path: "technologies/:slug", element: <PlaceholderPage title="Technology detail" /> },
      { path: "cv", element: <PlaceholderPage title="CV" /> }
    ]
  },
  {
    path: "/admin/login",
    element: <LoginPage />
  },
  {
    path: "/admin",
    element: <RequireAuth />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { index: true, element: <AdminDashboardPage /> },
          { path: "profile", element: <ProfilePage /> },
          { path: "projects", element: <PlaceholderPage title="Projects CMS" /> },
          { path: "notes", element: <PlaceholderPage title="Notes CMS" /> },
          { path: "technologies", element: <PlaceholderPage title="Technologies CMS" /> },
          { path: "categories", element: <PlaceholderPage title="Categories CMS" /> },
          { path: "tags", element: <PlaceholderPage title="Tags CMS" /> },
          { path: "skill-groups", element: <PlaceholderPage title="Skill groups CMS" /> },
          { path: "cv-files", element: <PlaceholderPage title="CV files CMS" /> },
          { path: "media", element: <PlaceholderPage title="Media library" /> },
          { path: "audit-logs", element: <PlaceholderPage title="Audit logs" /> }
        ]
      }
    ]
  },
  {
    path: "*",
    element: <NotFoundPage />
  }
]);
