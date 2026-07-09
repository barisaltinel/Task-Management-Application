import { apiRequest } from './client';

export async function loginUser(credentials) {
  return apiRequest('/auth/login', {
    method: 'POST',
    body: credentials,
  });
}

export async function registerUser(payload) {
  return apiRequest('/auth/register', {
    method: 'POST',
    body: payload,
  });
}

export async function logoutUser(token) {
  return apiRequest('/auth/logout', {
    method: 'POST',
    token,
  });
}

export async function fetchWorkspace(token) {
  const [tasks, projects, attachments, comments] = await Promise.allSettled([
    apiRequest('/tasks', { token }),
    apiRequest('/projects', { token }),
    apiRequest('/attachments', { token }),
    apiRequest('/comments', { token }),
  ]);

  return {
    tasks,
    projects,
    attachments,
    comments,
  };
}

export async function createTask(token, payload) {
  return apiRequest('/tasks', {
    method: 'POST',
    token,
    body: payload,
  });
}

export async function updateTask(token, taskId, payload) {
  return apiRequest(`/tasks/${taskId}`, {
    method: 'PUT',
    token,
    body: payload,
  });
}

export async function cancelTask(token, taskId, reason) {
  return apiRequest(
    `/tasks/${taskId}/cancel?reason=${encodeURIComponent(reason)}`,
    {
      method: 'PUT',
      token,
    }
  );
}

export async function createProject(token, payload) {
  return apiRequest('/projects', {
    method: 'POST',
    token,
    body: payload,
  });
}

export async function uploadAttachment(token, formData) {
  return apiRequest('/attachments', {
    method: 'POST',
    token,
    body: formData,
    isFormData: true,
  });
}

export async function createComment(token, payload) {
  return apiRequest('/comments', {
    method: 'POST',
    token,
    body: payload,
  });
}
