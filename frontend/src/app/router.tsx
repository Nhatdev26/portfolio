import { createBrowserRouter } from "react-router-dom";

import { RequireAuth } from "../features/auth/AuthProvider";
import { AdminLayout } from "../layouts/AdminLayout";
import { PublicLayout } from "../layouts/PublicLayout";
import { AdminDashboardPage } from "../pages/admin/AdminDashboardPage";
import { LoginPage } from "../pages/admin/LoginPage";
import { NoteFormPage } from "../pages/admin/NoteFormPage";
import { NoteListPage } from "../pages/admin/NoteListPage";
import { ProfilePage } from "../pages/admin/ProfilePage";
import { ProjectFormPage } from "../pages/admin/ProjectFormPage";
import { ProjectListPage } from "../pages/admin/ProjectListPage";
import { TaxonomyPage } from "../pages/admin/TaxonomyPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { PlaceholderPage } from "../pages/PlaceholderPage";
import { AboutPage } from "../pages/public/AboutPage";
import { HomePage } from "../pages/public/HomePage";
import { NoteDetailPage } from "../pages/public/NoteDetailPage";
import { NotesPage } from "../pages/public/NotesPage";
import { ProjectDetailPage } from "../pages/public/ProjectDetailPage";
import { ProjectsPage } from "../pages/public/ProjectsPage";
import { TechnologyDetailPage } from "../pages/public/TechnologyDetailPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <PublicLayout />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "about", element: <AboutPage /> },
      { path: "projects", element: <ProjectsPage /> },
      { path: "projects/:slug", element: <ProjectDetailPage /> },
      { path: "notes", element: <NotesPage /> },
      { path: "notes/:slug", element: <NoteDetailPage /> },
      { path: "technologies/:slug", element: <TechnologyDetailPage /> },
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
          { path: "projects", element: <ProjectListPage /> },
          { path: "projects/new", element: <ProjectFormPage /> },
          { path: "projects/:id/edit", element: <ProjectFormPage /> },
          { path: "notes", element: <NoteListPage /> },
          { path: "notes/new", element: <NoteFormPage /> },
          { path: "notes/:id/edit", element: <NoteFormPage /> },
          { path: "technologies", element: <TaxonomyPage section="technologies" /> },
          { path: "categories", element: <TaxonomyPage section="categories" /> },
          { path: "tags", element: <TaxonomyPage section="tags" /> },
          { path: "skill-groups", element: <TaxonomyPage section="skill-groups" /> },
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
