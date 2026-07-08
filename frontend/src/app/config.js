export const VIEWS = ["overview", "tasks", "projects", "files", "comments"];
export const DEFAULT_VIEW = "overview";

export const TASK_STATES = [
  "BACKLOG",
  "IN_ANALYSIS",
  "IN_DEVELOPMENT",
  "IN_PROGRESS",
  "BLOCKED",
  "CANCELLED",
  "COMPLETED"
];

export const TASK_PRIORITIES = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];
export const PROJECT_STATUSES = ["IN_PROGRESS", "COMPLETED", "CANCELLED"];

export const EMPTY_WORKSPACE = {
  tasks: [],
  projects: [],
  attachments: [],
  comments: []
};

export const EMPTY_LOGIN_FORM = { email: "", password: "" };
export const EMPTY_REGISTER_FORM = { name: "", email: "", password: "" };
export function buildEmptyTaskForm() {
  const today = new Date();
  const dueDate = new Date(today);
  dueDate.setDate(dueDate.getDate() + 7);

  return {
    title: "",
    description: "",
    priority: "MEDIUM",
    state: "BACKLOG",
    startDate: today.toISOString().slice(0, 10),
    dueDate: dueDate.toISOString().slice(0, 10),
    projectId: "",
    assigneeId: ""
  };
}
export const EMPTY_PROJECT_FORM = {
  title: "",
  description: "",
  departmentName: "",
  status: "IN_PROGRESS"
};
export const EMPTY_UPLOAD_FORM = { taskId: "", file: null };
export const EMPTY_COMMENT_FORM = { taskId: "", text: "" };

export function getAvailableViews(role, canManageProjects = false) {
  const normalizedRole = typeof role === "string" ? role.trim().toUpperCase() : "";

  if (canManageProjects || normalizedRole === "ADMIN" || normalizedRole === "PROJECT_MANAGER") {
    return VIEWS;
  }

  return VIEWS.filter((view) => view !== "projects");
}

export function getDefaultViewForRole(role, canManageProjects = false) {
  const normalizedRole = typeof role === "string" ? role.trim().toUpperCase() : "";

  if (canManageProjects || normalizedRole === "ADMIN" || normalizedRole === "PROJECT_MANAGER") {
    return "projects";
  }

  if (normalizedRole === "TEAM_LEADER" || normalizedRole === "TEAM_MEMBER") {
    return "tasks";
  }

  return DEFAULT_VIEW;
}
