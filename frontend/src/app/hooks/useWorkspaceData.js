import { useCallback, useEffect, useState } from 'react';
import {
  buildEmptyTaskForm,
  EMPTY_COMMENT_FORM,
  EMPTY_PROJECT_FORM,
  EMPTY_UPLOAD_FORM,
  EMPTY_WORKSPACE,
} from '../config';
import {
  cancelTask,
  createComment,
  createProject,
  createTask,
  fetchWorkspace,
  updateTask,
  uploadAttachment,
} from '../../shared/api/taskManagementApi';

export function useWorkspaceData({ session, showNotice, clearActiveSession }) {
  const [workspace, setWorkspace] = useState(EMPTY_WORKSPACE);
  const [canManageProjects, setCanManageProjects] = useState(false);
  const [loading, setLoading] = useState(false);
  const [taskForm, setTaskForm] = useState(buildEmptyTaskForm);
  const [projectForm, setProjectForm] = useState(EMPTY_PROJECT_FORM);
  const [uploadForm, setUploadForm] = useState(EMPTY_UPLOAD_FORM);
  const [commentForm, setCommentForm] = useState(EMPTY_COMMENT_FORM);

  const resetWorkspace = useCallback(() => {
    setWorkspace(EMPTY_WORKSPACE);
    setCanManageProjects(false);
  }, []);

  const handleProtectedError = useCallback(
    (error, fallbackMessage) => {
      if (error?.status === 401) {
        clearActiveSession(
          error.message || 'Your session ended. Sign in again to continue.'
        );
        return true;
      }

      showNotice({
        type: 'error',
        title: 'Request failed',
        text: error?.message || fallbackMessage,
      });
      return false;
    },
    [clearActiveSession, showNotice]
  );

  const loadWorkspace = useCallback(
    async (showSyncNotice = false) => {
      if (!session?.token) {
        resetWorkspace();
        return false;
      }

      setLoading(true);
      try {
        const snapshot = await fetchWorkspace(session.token);

        if (snapshot.tasks.status === 'rejected') {
          handleProtectedError(snapshot.tasks.reason, 'Authentication failed.');
          return false;
        }

        setWorkspace({
          tasks: snapshot.tasks.value || [],
          projects:
            snapshot.projects.status === 'fulfilled'
              ? snapshot.projects.value || []
              : [],
          attachments:
            snapshot.attachments.status === 'fulfilled'
              ? snapshot.attachments.value || []
              : [],
          comments:
            snapshot.comments.status === 'fulfilled'
              ? snapshot.comments.value || []
              : [],
        });
        setCanManageProjects(snapshot.projects.status === 'fulfilled');

        if (showSyncNotice) {
          showNotice({
            type: 'success',
            title: 'Workspace updated',
            text: 'Data synchronized.',
          });
        }

        return true;
      } finally {
        setLoading(false);
      }
    },
    [handleProtectedError, resetWorkspace, session?.token, showNotice]
  );

  const handleTaskCreate = useCallback(
    async (event) => {
      event.preventDefault();
      try {
        setLoading(true);
        await createTask(session.token, {
          title: taskForm.title.trim(),
          description: taskForm.description.trim(),
          priority: taskForm.priority,
          state: taskForm.state,
          startDate: taskForm.startDate || null,
          dueDate: taskForm.dueDate || null,
          projectId: taskForm.projectId ? Number(taskForm.projectId) : null,
          assigneeId: taskForm.assigneeId ? Number(taskForm.assigneeId) : null,
        });
        setTaskForm(buildEmptyTaskForm());
        const synced = await loadWorkspace();
        if (!synced) {
          return false;
        }
        showNotice({
          type: 'success',
          title: 'Task created',
          text: 'Your task was added successfully.',
        });
        return true;
      } catch (error) {
        handleProtectedError(error, 'Task creation failed.');
        return false;
      } finally {
        setLoading(false);
      }
    },
    [handleProtectedError, loadWorkspace, session?.token, showNotice, taskForm]
  );

  const handleTaskStateChange = useCallback(
    async (task, nextState) => {
      try {
        setLoading(true);
        await updateTask(session.token, task.id, {
          title: task.title,
          description: task.description,
          state: nextState,
          priority: task.priority,
          startDate: task.startDate || null,
          dueDate: task.dueDate || null,
          projectId: task.project?.id || null,
          assigneeId: task.assignee?.id || null,
        });
        await loadWorkspace();
      } catch (error) {
        handleProtectedError(error, 'Task update failed.');
      } finally {
        setLoading(false);
      }
    },
    [handleProtectedError, loadWorkspace, session?.token]
  );

  const handleTaskCancel = useCallback(
    async (taskId) => {
      const reason = window.prompt('Cancellation reason');
      if (!reason) {
        return;
      }

      try {
        setLoading(true);
        await cancelTask(session.token, taskId, reason);
        await loadWorkspace();
      } catch (error) {
        handleProtectedError(error, 'Task cancellation failed.');
      } finally {
        setLoading(false);
      }
    },
    [handleProtectedError, loadWorkspace, session?.token]
  );

  const handleProjectCreate = useCallback(
    async (event) => {
      event.preventDefault();
      try {
        setLoading(true);
        await createProject(session.token, projectForm);
        setProjectForm(EMPTY_PROJECT_FORM);
        await loadWorkspace();
      } catch (error) {
        handleProtectedError(error, 'Project creation failed.');
      } finally {
        setLoading(false);
      }
    },
    [handleProtectedError, loadWorkspace, projectForm, session?.token]
  );

  const handleUpload = useCallback(
    async (event) => {
      event.preventDefault();
      if (!uploadForm.file || !uploadForm.taskId) {
        return;
      }

      const formData = new FormData();
      formData.append('file', uploadForm.file);
      formData.append('taskId', uploadForm.taskId);

      try {
        setLoading(true);
        await uploadAttachment(session.token, formData);
        setUploadForm(EMPTY_UPLOAD_FORM);
        await loadWorkspace();
      } catch (error) {
        handleProtectedError(error, 'Upload failed.');
      } finally {
        setLoading(false);
      }
    },
    [handleProtectedError, loadWorkspace, session?.token, uploadForm]
  );

  const handleCommentCreate = useCallback(
    async (event) => {
      event.preventDefault();
      if (!commentForm.taskId || !commentForm.text.trim()) {
        return;
      }

      try {
        setLoading(true);
        await createComment(session.token, {
          text: commentForm.text.trim(),
          taskId: Number(commentForm.taskId),
        });
        setCommentForm(EMPTY_COMMENT_FORM);
        await loadWorkspace();
      } catch (error) {
        handleProtectedError(error, 'Comment failed.');
      } finally {
        setLoading(false);
      }
    },
    [commentForm, handleProtectedError, loadWorkspace, session?.token]
  );

  useEffect(() => {
    if (session?.token) {
      loadWorkspace();
      return;
    }

    resetWorkspace();
  }, [loadWorkspace, resetWorkspace, session?.token]);

  return {
    workspace,
    canManageProjects,
    loading,
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
  };
}
