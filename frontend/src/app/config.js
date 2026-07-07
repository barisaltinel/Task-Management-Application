export const VIEWS = ["overview", "tasks", "projects", "files", "comments"];

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
export const EMPTY_TASK_FORM = {
  title: "",
  description: "",
  priority: "MEDIUM",
  state: "BACKLOG",
  projectId: "",
  assigneeId: ""
};
export const EMPTY_PROJECT_FORM = {
  title: "",
  description: "",
  departmentName: "",
  status: "IN_PROGRESS"
};
export const EMPTY_UPLOAD_FORM = { taskId: "", file: null };
export const EMPTY_COMMENT_FORM = { taskId: "", text: "" };
