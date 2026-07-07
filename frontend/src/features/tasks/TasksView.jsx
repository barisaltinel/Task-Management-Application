import { useEffect, useState } from "react";
import { TASK_PRIORITIES, TASK_STATES } from "../../app/config";
import { humanize } from "../../shared/utils/format";

export default function TasksView({
  taskForm,
  setTaskForm,
  onTaskCreate,
  loading,
  board,
  onTaskStateChange,
  onTaskCancel
}) {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

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
                  </div>

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
                  <input
                    type="number"
                    value={taskForm.projectId}
                    onChange={(event) =>
                      setTaskForm((prev) => ({ ...prev, projectId: event.target.value }))
                    }
                  />
                </label>

                <label>
                  Assignee ID
                  <input
                    type="number"
                    value={taskForm.assigneeId}
                    onChange={(event) =>
                      setTaskForm((prev) => ({ ...prev, assigneeId: event.target.value }))
                    }
                  />
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
