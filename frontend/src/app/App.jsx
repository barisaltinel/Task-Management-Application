import { useEffect, useMemo, useState } from "react";
import Sidebar from "../components/layout/Sidebar";
import TopHeader from "../components/layout/TopHeader";
import MobileNav from "../components/layout/MobileNav";
import Notice from "../components/common/Notice";
import AuthView from "../features/auth/AuthView";
import OverviewView from "../features/overview/OverviewView";
import TasksView from "../features/tasks/TasksView";
import ProjectsView from "../features/projects/ProjectsView";
import FilesView from "../features/files/FilesView";
import CommentsView from "../features/comments/CommentsView";
import { apiRequest } from "../shared/api/client";
import {
  clearSession,
  createSession,
  readSession,
  saveSession
} from "../shared/utils/session";
import {
  EMPTY_COMMENT_FORM,
  EMPTY_LOGIN_FORM,
  EMPTY_PROJECT_FORM,
  EMPTY_REGISTER_FORM,
  EMPTY_TASK_FORM,
  EMPTY_UPLOAD_FORM,
  EMPTY_WORKSPACE,
  TASK_STATES
} from "./config";

export default function App() {
  const [session, setSession] = useState(readSession);
  const [view, setView] = useState("overview");
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState({ type: "", text: "" });
  const [authMode, setAuthMode] = useState("login");
  const [workspace, setWorkspace] = useState(EMPTY_WORKSPACE);
  const [canManageProjects, setCanManageProjects] = useState(false);

  const [loginForm, setLoginForm] = useState(EMPTY_LOGIN_FORM);
  const [registerForm, setRegisterForm] = useState(EMPTY_REGISTER_FORM);
  const [taskForm, setTaskForm] = useState(EMPTY_TASK_FORM);
  const [projectForm, setProjectForm] = useState(EMPTY_PROJECT_FORM);
  const [uploadForm, setUploadForm] = useState(EMPTY_UPLOAD_FORM);
  const [commentForm, setCommentForm] = useState(EMPTY_COMMENT_FORM);

  const metrics = useMemo(
    () => ({
      total: workspace.tasks.length,
      done: workspace.tasks.filter((task) => task.state === "COMPLETED").length,
      blocked: workspace.tasks.filter((task) => task.state === "BLOCKED").length,
      critical: workspace.tasks.filter((task) => task.priority === "CRITICAL").length
    }),
    [workspace.tasks]
  );

  const board = useMemo(
    () =>
      TASK_STATES.map((state) => ({
        state,
        tasks: workspace.tasks.filter((task) => task.state === state)
      })),
    [workspace.tasks]
  );

  function clearActiveSession(message = "Your session has expired. Please sign in again.") {
    clearSession();
    setSession(null);
    setWorkspace(EMPTY_WORKSPACE);
    setCanManageProjects(false);
    setView("overview");
    setNotice({ type: "info", text: message });
  }

  function handleProtectedError(error, fallbackMessage) {
    if (error?.status === 401) {
      clearActiveSession(
        error.message || "Your session has expired. Please sign in again."
      );
      return true;
    }

    setNotice({ type: "error", text: error?.message || fallbackMessage });
    return false;
  }

  async function loadWorkspace(token, showSyncNotice = false) {
    setLoading(true);
    const [tasks, projects, attachments, comments] = await Promise.allSettled([
      apiRequest("/tasks", { token }),
      apiRequest("/projects", { token }),
      apiRequest("/attachments", { token }),
      apiRequest("/comments", { token })
    ]);

    if (tasks.status === "rejected") {
      handleProtectedError(tasks.reason, "Authentication failed.");
      setLoading(false);
      return false;
    }

    setWorkspace({
      tasks: tasks.value || [],
      projects: projects.status === "fulfilled" ? projects.value || [] : [],
      attachments: attachments.status === "fulfilled" ? attachments.value || [] : [],
      comments: comments.status === "fulfilled" ? comments.value || [] : []
    });

    setCanManageProjects(projects.status === "fulfilled");
    if (showSyncNotice) {
      setNotice({ type: "success", text: "Data synchronized." });
    }

    setLoading(false);
    return true;
  }

  async function handleLogin(event) {
    event.preventDefault();
    try {
      setLoading(true);
      const authPayload = await apiRequest("/auth/login", {
        method: "POST",
        body: loginForm
      });
      const nextSession = createSession(authPayload, loginForm.email);
      saveSession(nextSession);
      setSession(nextSession);
      setLoginForm(EMPTY_LOGIN_FORM);
      setNotice({ type: "success", text: "Welcome back." });
    } catch (error) {
      setNotice({ type: "error", text: error.message || "Sign in failed." });
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    try {
      setLoading(true);
      await apiRequest("/auth/register", {
        method: "POST",
        body: registerForm
      });
      setAuthMode("login");
      setRegisterForm(EMPTY_REGISTER_FORM);
      setNotice({ type: "success", text: "Account created. Please sign in." });
    } catch (error) {
      setNotice({ type: "error", text: error.message || "Registration failed." });
    } finally {
      setLoading(false);
    }
  }

  function handleLogout() {
    if (session?.token) {
      apiRequest("/auth/logout", {
        method: "POST",
        token: session.token
      }).catch(() => {});
    }
    clearSession();
    setSession(null);
    setWorkspace(EMPTY_WORKSPACE);
    setCanManageProjects(false);
    setView("overview");
    setNotice({ type: "info", text: "Logged out." });
  }

  async function handleTaskCreate(event) {
    event.preventDefault();
    try {
      setLoading(true);
      await apiRequest("/tasks", {
        method: "POST",
        token: session.token,
        body: {
          title: taskForm.title.trim(),
          description: taskForm.description.trim(),
          priority: taskForm.priority,
          state: taskForm.state,
          projectId: taskForm.projectId ? Number(taskForm.projectId) : null,
          assigneeId: taskForm.assigneeId ? Number(taskForm.assigneeId) : null
        }
      });
      setTaskForm(EMPTY_TASK_FORM);
      const synced = await loadWorkspace(session.token);
      if (!synced) {
        return;
      }
      setNotice({ type: "success", text: "Task created." });
    } catch (error) {
      handleProtectedError(error, "Task creation failed.");
    } finally {
      setLoading(false);
    }
  }

  async function handleTaskStateChange(task, nextState) {
    try {
      setLoading(true);
      await apiRequest(`/tasks/${task.id}`, {
        method: "PUT",
        token: session.token,
        body: {
          title: task.title,
          description: task.description,
          state: nextState,
          priority: task.priority,
          projectId: task.project?.id || null,
          assigneeId: task.assignee?.id || null
        }
      });
      await loadWorkspace(session.token);
    } catch (error) {
      handleProtectedError(error, "Task update failed.");
    } finally {
      setLoading(false);
    }
  }

  async function handleTaskCancel(taskId) {
    const reason = window.prompt("Cancellation reason");
    if (!reason) return;
    try {
      setLoading(true);
      await apiRequest(`/tasks/${taskId}/cancel?reason=${encodeURIComponent(reason)}`, {
        method: "PUT",
        token: session.token
      });
      await loadWorkspace(session.token);
    } catch (error) {
      handleProtectedError(error, "Task cancellation failed.");
    } finally {
      setLoading(false);
    }
  }

  async function handleProjectCreate(event) {
    event.preventDefault();
    try {
      setLoading(true);
      await apiRequest("/projects", {
        method: "POST",
        token: session.token,
        body: projectForm
      });
      setProjectForm(EMPTY_PROJECT_FORM);
      await loadWorkspace(session.token);
    } catch (error) {
      handleProtectedError(error, "Project creation failed.");
    } finally {
      setLoading(false);
    }
  }

  async function handleUpload(event) {
    event.preventDefault();
    if (!uploadForm.file || !uploadForm.taskId) return;

    const formData = new FormData();
    formData.append("file", uploadForm.file);
    formData.append("taskId", uploadForm.taskId);

    try {
      setLoading(true);
      await apiRequest("/attachments", {
        method: "POST",
        token: session.token,
        body: formData,
        isFormData: true
      });
      setUploadForm(EMPTY_UPLOAD_FORM);
      await loadWorkspace(session.token);
    } catch (error) {
      handleProtectedError(error, "Upload failed.");
    } finally {
      setLoading(false);
    }
  }

  async function handleCommentCreate(event) {
    event.preventDefault();
    if (!commentForm.taskId || !commentForm.text.trim()) return;

    try {
      setLoading(true);
      await apiRequest("/comments", {
        method: "POST",
        token: session.token,
        body: {
          text: commentForm.text.trim(),
          taskId: Number(commentForm.taskId)
        }
      });
      setCommentForm(EMPTY_COMMENT_FORM);
      await loadWorkspace(session.token);
    } catch (error) {
      handleProtectedError(error, "Comment failed.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (session?.token) {
      loadWorkspace(session.token);
    }
  }, [session?.token]);

  if (!session?.token) {
    return (
      <AuthView
        authMode={authMode}
        setAuthMode={setAuthMode}
        loginForm={loginForm}
        setLoginForm={setLoginForm}
        registerForm={registerForm}
        setRegisterForm={setRegisterForm}
        loading={loading}
        onLogin={handleLogin}
        onRegister={handleRegister}
        notice={notice}
      />
    );
  }

  return (
    <div className="app-shell">
      <Sidebar
        email={session.email || session.name}
        view={view}
        setView={setView}
        metrics={metrics}
      />

      <main className="main-panel">
        <TopHeader
          onRefresh={() => loadWorkspace(session.token, true)}
          onLogout={handleLogout}
        />
        <MobileNav view={view} setView={setView} />
        <Notice notice={notice} />

        {view === "overview" && <OverviewView metrics={metrics} />}

        {view === "tasks" && (
          <TasksView
            taskForm={taskForm}
            setTaskForm={setTaskForm}
            onTaskCreate={handleTaskCreate}
            loading={loading}
            board={board}
            onTaskStateChange={handleTaskStateChange}
            onTaskCancel={handleTaskCancel}
          />
        )}

        {view === "projects" && (
          <ProjectsView
            projectForm={projectForm}
            setProjectForm={setProjectForm}
            onProjectCreate={handleProjectCreate}
            canManageProjects={canManageProjects}
            loading={loading}
            projects={workspace.projects}
          />
        )}

        {view === "files" && (
          <FilesView
            uploadForm={uploadForm}
            setUploadForm={setUploadForm}
            onUpload={handleUpload}
            loading={loading}
            attachments={workspace.attachments}
          />
        )}

        {view === "comments" && (
          <CommentsView
            commentForm={commentForm}
            setCommentForm={setCommentForm}
            onCommentCreate={handleCommentCreate}
            loading={loading}
            comments={workspace.comments}
          />
        )}
      </main>
    </div>
  );
}
