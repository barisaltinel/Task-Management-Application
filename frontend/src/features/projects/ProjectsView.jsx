import { PROJECT_STATUSES } from "../../app/config";
import { humanize } from "../../shared/utils/format";

export default function ProjectsView({
  projectForm,
  setProjectForm,
  onProjectCreate,
  canManageProjects,
  loading,
  projects
}) {
  return (
    <section className="split-view">
      <article className="panel-card">
        <header>
          <h3>Create Project</h3>
          <span>{canManageProjects ? "enabled" : "limited"}</span>
        </header>

        <form className="stack-form" onSubmit={onProjectCreate}>
          <label>
            Title
            <input
              disabled={!canManageProjects}
              value={projectForm.title}
              onChange={(event) =>
                setProjectForm((prev) => ({ ...prev, title: event.target.value }))
              }
            />
          </label>

          <label>
            Description
            <textarea
              rows={3}
              disabled={!canManageProjects}
              value={projectForm.description}
              onChange={(event) =>
                setProjectForm((prev) => ({ ...prev, description: event.target.value }))
              }
            />
          </label>

          <div className="form-grid">
            <label>
              Department
              <input
                disabled={!canManageProjects}
                value={projectForm.departmentName}
                onChange={(event) =>
                  setProjectForm((prev) => ({
                    ...prev,
                    departmentName: event.target.value
                  }))
                }
              />
            </label>

            <label>
              Status
              <select
                disabled={!canManageProjects}
                value={projectForm.status}
                onChange={(event) =>
                  setProjectForm((prev) => ({ ...prev, status: event.target.value }))
                }
              >
                {PROJECT_STATUSES.map((value) => (
                  <option key={value} value={value}>
                    {humanize(value)}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <button className="primary-btn" disabled={loading || !canManageProjects}>
            Create Project
          </button>
        </form>
      </article>

      <article className="panel-card">
        <header>
          <h3>Project Catalog</h3>
          <span>{projects.length} active</span>
        </header>

        <div className="tile-grid">
          {projects.map((project) => (
            <div className="project-tile" key={project.id}>
              <strong>{project.title}</strong>
              <p>{project.description}</p>
              <div className="tile-meta">
                <span className={`pill state-${project.status.toLowerCase()}`}>
                  {humanize(project.status)}
                </span>
                <span>{project.departmentName}</span>
              </div>
            </div>
          ))}

          {!projects.length && (
            <p className="muted">No project access or no project found.</p>
          )}
        </div>
      </article>
    </section>
  );
}
