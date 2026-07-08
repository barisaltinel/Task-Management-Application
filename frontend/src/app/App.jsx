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
  DEFAULT_VIEW,
  EMPTY_COMMENT_FORM,
  EMPTY_LOGIN_FORM,
  EMPTY_PROJECT_FORM,
  EMPTY_REGISTER_FORM,
  EMPTY_TASK_FORM,
  EMPTY_UPLOAD_FORM,
  EMPTY_WORKSPACE,
  getAvailableViews,
  getDefaultViewForRole,
  TASK_STATES
} from "./config";

export default function App() {
  const [session, setSession] = useState(readSession);
  const [view, setView] = useState(DEFAULT_VIEW);
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
  const role = session?.user?.role || "";
  const availableViews = useMemo(
    () => getAvailableViews(role, canManageProjects),
    [role, canManageProjects]
  );

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

  function showNotice(nextNotice) {
    setNotice((currentNotice) => ({
      title: "",
      ...currentNotice,
      ...nextNotice
    }));
  }

  function dismissNotice() {
    setNotice({ type: "", text: "", title: "" });
  }

  function clearActiveSession(message = "Your session has expired. Please sign in again.") {
    clearSession();
    setSession(null);
    setWorkspace(EMPTY_WORKSPACE);
    setCanManageProjects(false);
    setView(DEFAULT_VIEW);
    setAuthMode("login");
    showNotice({
      type: "info",
      title: "Session ended",
      text: message
    });
  }

  function handleProtectedError(error, fallbackMessage) {
    if (error?.status === 401) {
      clearActiveSession(
        error.message || "Your session ended. Sign in again to continue."
      );
      return true;
    }

    showNotice({
      type: "error",
      title: "Request failed",
      text: error?.message || fallbackMessage
    });
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
      showNotice({
        type: "success",
        title: "Workspace updated",
        text: "Data synchronized."
      });
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
      setView(getDefaultViewForRole(nextSession.user?.role));
      setLoginForm(EMPTY_LOGIN_FORM);
      showNotice({
        type: "success",
        title: "Signed in",
        text: "Welcome back. Your workspace is syncing now."
      });
    } catch (error) {
      showNotice({
        type: "error",
        title: "Sign-in failed",
        text: error.message || "Sign in failed."
      });
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
      showNotice({
        type: "success",
        title: "Account created",
        text: "Please sign in to open your workspace."
      });
    } catch (error) {
      showNotice({
        type: "error",
        title: "Registration failed",
        text: error.message || "Registration failed."
      });
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
    setView(DEFAULT_VIEW);
    setAuthMode("login");
    showNotice({
      type: "info",
      title: "Signed out",
      text: "You have signed out of this tab."
    });
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
      showNotice({
        type: "success",
        title: "Task created",
        text: "Your task was added successfully."
      });
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

  useEffect(() => {
    if (!availableViews.includes(view)) {
      setView(getDefaultViewForRole(role, canManageProjects));
    }
  }, [availableViews, canManageProjects, role, view]);

  useEffect(() => {
    if (!notice?.text || notice.type === "error") {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      dismissNotice();
    }, 4200);

    return () => window.clearTimeout(timeoutId);
  }, [notice]);

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
        onDismissNotice={dismissNotice}
      />
    );
  }

  return (
    <div className="app-shell">
      <Sidebar
        email={session.email || session.name}
        role={role}
        view={view}
        setView={setView}
        views={availableViews}
        metrics={metrics}
      />

      <main className="main-panel">
        <TopHeader
          role={role}
          onRefresh={() => loadWorkspace(session.token, true)}
          onLogout={handleLogout}
        />
        <MobileNav view={view} setView={setView} views={availableViews} />
        <Notice notice={notice} onDismiss={dismissNotice} />

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
