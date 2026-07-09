import { useEffect, useMemo, useState } from "react";
import { TASK_PRIORITIES, TASK_STATES } from "../../app/config";
import { humanize } from "../../shared/utils/format";

export default function TasksView({
  taskForm,
  setTaskForm,
  onTaskCreate,
  loading,
  tasks,
  projects,
  users,
  onTaskStateChange,
  onTaskCancel,
}) {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [stateFilter, setStateFilter] = useState("ALL");
  const [priorityFilter, setPriorityFilter] = useState("ALL");
  const [projectFilter, setProjectFilter] = useState("ALL");
  const [assigneeFilter, setAssigneeFilter] = useState("ALL");
  const [focusMode, setFocusMode] = useState(false);

  const filteredTasks = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();

    return tasks.filter((task) => {
      const matchesQuery =
        !query ||
        task.title.toLowerCase().includes(query) ||
        task.description.toLowerCase().includes(query) ||
        (task.project?.title || "").toLowerCase().includes(query) ||
        (task.assignee?.name || "").toLowerCase().includes(query);
      const matchesState = stateFilter === "ALL" || task.state === stateFilter;
      const matchesPriority = priorityFilter === "ALL" || task.priority === priorityFilter;
      const matchesProject =
        projectFilter === "ALL" || String(task.project?.id || "") === projectFilter;
      const matchesAssignee =
        assigneeFilter === "ALL" || String(task.assignee?.id || "") === assigneeFilter;
      const matchesFocus =
        !focusMode ||
        task.state === "BLOCKED" ||
        (task.priority === "CRITICAL" && task.state !== "COMPLETED") ||
        task.state === "CANCELLED";

      return (
        matchesQuery &&
        matchesState &&
        matchesPriority &&
        matchesProject &&
        matchesAssignee &&
        matchesFocus
      );
    });
  }, [assigneeFilter, focusMode, priorityFilter, projectFilter, searchTerm, stateFilter, tasks]);

  const board = useMemo(
    () =>
      TASK_STATES.map((state) => ({
        state,
        tasks: filteredTasks.filter((task) => task.state === state),
      })),
    [filteredTasks],
  );

  const flaggedTasks = useMemo(
    () =>
      filteredTasks.filter(
        (task) =>
          task.state === "BLOCKED" ||
          (task.priority === "CRITICAL" && task.state !== "COMPLETED") ||
          task.state === "CANCELLED",
      ),
    [filteredTasks],
  );

  useEffect(() => {
    if (!isCreateModalOpen) return undefined;
    const onEscape = (event) => {
      if (event.key === "Escape") {
        setIsCreateModalOpen(false);
      }
    };
    window.addEventListener("keydown", onEscape);
    return () => window.removeEventListener("keydown", onEscape);
  }, [isCreateModalOpen]);

  return (
    <section className="tasks-view reveal">
      <article className="panel-card task-command">
        <header>
          <div>
            <h3>Task Command Center</h3>
            <span>
              {filteredTasks.length} matching task{filteredTasks.length === 1 ? "" : "s"} out of{" "}
              {tasks.length}
            </span>
          </div>
          <button
            className={`ghost-btn small ${focusMode ? "task-filter-btn active-filter" : ""}`}
            type="button"
            onClick={() => setFocusMode((current) => !current)}
          >
            {focusMode ? "Show all tasks" : "Focus on risks"}
          </button>
        </header>

        <div className="task-toolbar">
          <label className="toolbar-field toolbar-field--wide">
            Search
            <input
              placeholder="Task, assignee, or project"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />
          </label>

          <label className="toolbar-field">
            State
            <select
              className="modern-select"
              value={stateFilter}
              onChange={(event) => setStateFilter(event.target.value)}
            >
              <option value="ALL">All states</option>
              {TASK_STATES.map((value) => (
                <option key={value} value={value}>
                  {humanize(value)}
                </option>
              ))}
            </select>
          </label>

          <label className="toolbar-field">
            Priority
            <select
              className="modern-select"
              value={priorityFilter}
              onChange={(event) => setPriorityFilter(event.target.value)}
            >
              <option value="ALL">All priorities</option>
              {TASK_PRIORITIES.map((value) => (
                <option key={value} value={value}>
                  {humanize(value)}
                </option>
              ))}
            </select>
          </label>

          <label className="toolbar-field">
            Project
            <select
              className="modern-select"
              value={projectFilter}
              onChange={(event) => setProjectFilter(event.target.value)}
            >
              <option value="ALL">All projects</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.title}
                </option>
              ))}
            </select>
          </label>

          <label className="toolbar-field">
            Assignee
            <select
              className="modern-select"
              value={assigneeFilter}
              onChange={(event) => setAssigneeFilter(event.target.value)}
            >
              <option value="ALL">All assignees</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
          </label>
        </div>
      </article>

      {!!flaggedTasks.length && (
        <article className="panel-card task-alerts">
          <header>
            <h3>Attention Queue</h3>
            <span>{flaggedTasks.length} flagged items</span>
          </header>

          <div className="alert-grid">
            {flaggedTasks.slice(0, 4).map((task) => (
              <div className="alert-card" key={task.id}>
                <div className="task-meta">
                  <span className={`pill state-${task.state.toLowerCase()}`}>
                    {humanize(task.state)}
                  </span>
                  <span className={`pill priority-${task.priority.toLowerCase()}`}>
                    {humanize(task.priority)}
                  </span>
                </div>
                <strong>{task.title}</strong>
                <p>
                  {task.project?.title || "No project"} | {task.assignee?.name || "No assignee"}
                </p>
              </div>
            ))}
          </div>
        </article>
      )}

      <article className="panel-card tasks-view__board">
        <header>
          <h3>Task Board</h3>
          <button
            className="primary-btn create-task-btn"
            type="button"
            onClick={() => setIsCreateModalOpen(true)}
            disabled={loading}
          >
            Create Task
          </button>
        </header>

        <div className="board-grid">
          {board.map((column) => (
            <div className="state-column" key={column.state}>
              <h4>
                {humanize(column.state)}
                <span className="state-count">{column.tasks.length}</span>
              </h4>

              {column.tasks.map((task) => (
                <article key={task.id} className="task-card">
                  <strong>{task.title}</strong>
                  <p>{task.description}</p>

                  <div className="task-meta">
                    <span className={`pill priority-${task.priority.toLowerCase()}`}>
                      {humanize(task.priority)}
                    </span>
                    <span className="pill neutral">{task.project?.title || "No project"}</span>
                    <span className="pill neutral">{task.assignee?.name || "No assignee"}</span>
                  </div>

                  {task.reason && <small className="task-note">Reason: {task.reason}</small>}

                  <label className="inline-select">
                    Update state
                    <select
                      className="modern-select compact-select"
                      value={task.state}
                      onChange={(event) => onTaskStateChange(task, event.target.value)}
                    >
                      {TASK_STATES.map((value) => (
                        <option key={value} value={value}>
                          {humanize(value)}
                        </option>
                      ))}
                    </select>
                  </label>

                  <button
                    className="ghost-btn small"
                    type="button"
                    onClick={() => onTaskCancel(task.id)}
                  >
                    Cancel
                  </button>
                </article>
              ))}

              {!column.tasks.length && <p className="muted center">No task</p>}
            </div>
          ))}
        </div>
      </article>

      {isCreateModalOpen && (
        <div
          className="modal-backdrop"
          role="presentation"
          onClick={() => setIsCreateModalOpen(false)}
        >
          <article
            className="task-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-task-title"
            onClick={(event) => event.stopPropagation()}
          >
            <header className="task-modal__header">
              <h3 id="create-task-title">Create Task</h3>
              <button
                className="ghost-btn small"
                type="button"
                onClick={() => setIsCreateModalOpen(false)}
              >
                Close
              </button>
            </header>

            <form className="stack-form stack-form--task" onSubmit={onTaskCreate}>
              <label>
                Title
                <input
                  value={taskForm.title}
                  onChange={(event) =>
                    setTaskForm((prev) => ({ ...prev, title: event.target.value }))
                  }
                />
              </label>

              <label>
                Description
                <textarea
                  rows={3}
                  value={taskForm.description}
                  onChange={(event) =>
                    setTaskForm((prev) => ({ ...prev, description: event.target.value }))
                  }
                />
              </label>

              <div className="form-grid">
                <label>
                  Priority
                  <select
                    className="modern-select"
                    value={taskForm.priority}
                    onChange={(event) =>
                      setTaskForm((prev) => ({ ...prev, priority: event.target.value }))
                    }
                  >
                    {TASK_PRIORITIES.map((value) => (
                      <option key={value} value={value}>
                        {humanize(value)}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  State
                  <select
                    className="modern-select"
                    value={taskForm.state}
                    onChange={(event) =>
                      setTaskForm((prev) => ({ ...prev, state: event.target.value }))
                    }
                  >
                    {TASK_STATES.map((value) => (
                      <option key={value} value={value}>
                        {humanize(value)}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="form-grid">
                <label>
                  Project ID
                  <select
                    className="modern-select"
                    value={taskForm.projectId}
                    onChange={(event) =>
                      setTaskForm((prev) => ({ ...prev, projectId: event.target.value }))
                    }
                  >
                    <option value="">Select project</option>
                    {projects.map((project) => (
                      <option key={project.id} value={project.id}>
                        {project.title}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  Assignee
                  <select
                    className="modern-select"
                    value={taskForm.assigneeId}
                    onChange={(event) =>
                      setTaskForm((prev) => ({ ...prev, assigneeId: event.target.value }))
                    }
                  >
                    <option value="">Select assignee</option>
                    {users.map((user) => (
                      <option key={user.id} value={user.id}>
                        {user.name} ({humanize(user.role)})
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="task-modal__actions">
                <button
                  className="ghost-btn"
                  type="button"
                  onClick={() => setIsCreateModalOpen(false)}
                >
                  Cancel
                </button>
                <button className="primary-btn" disabled={loading} type="submit">
                  Create Task
                </button>
              </div>
            </form>
          </article>
        </div>
      )}
    </section>
  );
}
