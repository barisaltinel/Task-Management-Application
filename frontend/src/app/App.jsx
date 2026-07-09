import Sidebar from '../components/layout/Sidebar';
import TopHeader from '../components/layout/TopHeader';
import MobileNav from '../components/layout/MobileNav';
import Notice from '../components/common/Notice';
import AuthView from '../features/auth/AuthView';
import OverviewView from '../features/overview/OverviewView';
import TasksView from '../features/tasks/TasksView';
import ProjectsView from '../features/projects/ProjectsView';
import FilesView from '../features/files/FilesView';
import CommentsView from '../features/comments/CommentsView';
import { useNotice } from './hooks/useNotice';
import { useWorkspaceData } from './hooks/useWorkspaceData';
import { useWorkspaceInsights } from './hooks/useWorkspaceInsights';
import { useWorkspaceSession } from './hooks/useWorkspaceSession';
import { useWorkspaceView } from './hooks/useWorkspaceView';

export default function App() {
  const { notice, showNotice, dismissNotice } = useNotice();
  const {
    session,
    role,
    authMode,
    setAuthMode,
    loginForm,
    setLoginForm,
    registerForm,
    setRegisterForm,
    loading: authLoading,
    handleLogin,
    handleRegister,
    handleLogout,
    clearActiveSession,
  } = useWorkspaceSession(showNotice);
  const {
    workspace,
    canManageProjects,
    loading: workspaceLoading,
    taskForm,
    setTaskForm,
    projectForm,
    setProjectForm,
    uploadForm,
    setUploadForm,
    commentForm,
    setCommentForm,
    loadWorkspace,
    handleTaskCreate,
    handleTaskStateChange,
    handleTaskCancel,
    handleProjectCreate,
    handleUpload,
    handleCommentCreate,
  } = useWorkspaceData({
    session,
    showNotice,
    clearActiveSession,
  });
  const { view, setView, availableViews } = useWorkspaceView(
    role,
    canManageProjects
  );
  const { selectableUsers, projectSnapshots, metrics, overview } =
    useWorkspaceInsights(workspace);

  const loading = authLoading || workspaceLoading;

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
          overview={overview}
          onRefresh={() => loadWorkspace(true)}
          onLogout={handleLogout}
        />
        <MobileNav view={view} setView={setView} views={availableViews} />
        <Notice notice={notice} onDismiss={dismissNotice} />

        {view === 'overview' && (
          <OverviewView metrics={metrics} overview={overview} />
        )}

        {view === 'tasks' && (
          <TasksView
            taskForm={taskForm}
            setTaskForm={setTaskForm}
            onTaskCreate={handleTaskCreate}
            loading={loading}
            tasks={workspace.tasks}
            projects={workspace.projects}
            users={selectableUsers}
            onTaskStateChange={handleTaskStateChange}
            onTaskCancel={handleTaskCancel}
          />
        )}

        {view === 'projects' && (
          <ProjectsView
            projectForm={projectForm}
            setProjectForm={setProjectForm}
            onProjectCreate={handleProjectCreate}
            canManageProjects={canManageProjects}
            loading={loading}
            projects={workspace.projects}
            projectSnapshots={projectSnapshots}
          />
        )}

        {view === 'files' && (
          <FilesView
            uploadForm={uploadForm}
            setUploadForm={setUploadForm}
            onUpload={handleUpload}
            loading={loading}
            tasks={workspace.tasks}
            attachments={workspace.attachments}
          />
        )}

        {view === 'comments' && (
          <CommentsView
            commentForm={commentForm}
            setCommentForm={setCommentForm}
            onCommentCreate={handleCommentCreate}
            loading={loading}
            tasks={workspace.tasks}
            comments={workspace.comments}
          />
        )}
      </main>
    </div>
  );
}
