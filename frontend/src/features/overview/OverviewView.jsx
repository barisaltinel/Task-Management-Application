import { humanize } from "../../shared/utils/format";

export default function OverviewView({ metrics, overview }) {
  return (
    <section className="dashboard-stack reveal">
      <article className="panel-card overview-hero">
        <div>
          <span className="eyebrow">Executive Snapshot</span>
          <h3>{overview.pulseLabel}</h3>
          <p>
            {overview.completionRate}% of the visible work is complete, with{" "}
            {overview.atRiskCount} item{overview.atRiskCount === 1 ? "" : "s"} currently
            needing attention.
          </p>
        </div>

        <div className="hero-metrics">
          <div className="hero-metric">
            <small>Completion</small>
            <strong>{overview.completionRate}%</strong>
          </div>
          <div className="hero-metric">
            <small>Active Projects</small>
            <strong>{metrics.activeProjects}</strong>
          </div>
          <div className="hero-metric">
            <small>Collaboration</small>
            <strong>{overview.collaborationVolume}</strong>
          </div>
        </div>
      </article>

      <section className="view-grid">
        <article className="stat-card">
          <span>Total Tasks</span>
          <strong>{metrics.total}</strong>
        </article>
        <article className="stat-card">
          <span>Completed</span>
          <strong>{metrics.done}</strong>
        </article>
        <article className="stat-card">
          <span>Blocked</span>
          <strong>{metrics.blocked}</strong>
        </article>
        <article className="stat-card">
          <span>Critical</span>
          <strong>{metrics.critical}</strong>
        </article>
      </section>

      <section className="insight-grid">
        <article className="panel-card">
          <header>
            <h3>Project Health</h3>
            <span>Delivery confidence</span>
          </header>

          <div className="list-stack">
            {overview.projectSnapshots.map((project) => (
              <div className="health-row" key={project.id}>
                <div>
                  <strong>{project.title}</strong>
                  <small>
                    {project.taskCount} tasks | {project.teamCount} teammates
                  </small>
                </div>

                <div className="health-row__meta">
                  <div className="health-bar">
                    <span style={{ width: `${project.completionRate}%` }} />
                  </div>
                  <small>{project.completionRate}% done</small>
                </div>
              </div>
            ))}

            {!overview.projectSnapshots.length && (
              <p className="muted">No project data is available yet.</p>
            )}
          </div>
        </article>

        <article className="panel-card">
          <header>
            <h3>Risk Watch</h3>
            <span>{overview.atRiskCount} flagged</span>
          </header>

          <div className="list-stack">
            {overview.atRiskTasks.map((task) => (
              <div className="list-row list-row--top" key={task.id}>
                <div>
                  <strong>{task.title}</strong>
                  <small>
                    {task.project?.title || "Unassigned project"} | {humanize(task.state)}
                  </small>
                </div>
                <span className={`pill state-${task.state.toLowerCase()}`}>
                  {humanize(task.priority)}
                </span>
              </div>
            ))}

            {!overview.atRiskTasks.length && (
              <p className="muted">No blocked or high-risk items right now.</p>
            )}
          </div>
        </article>

        <article className="panel-card">
          <header>
            <h3>Team Load</h3>
            <span>Visible assignments</span>
          </header>

          <div className="list-stack">
            {overview.teamLoad.map((member) => (
              <div className="list-row list-row--top" key={member.id}>
                <div>
                  <strong>{member.name}</strong>
                  <small>
                    {member.activeCount} active | {member.blockedCount} blocked |{" "}
                    {member.criticalCount} critical
                  </small>
                </div>
                <span className="pill neutral">{humanize(member.role)}</span>
              </div>
            ))}

            {!overview.teamLoad.length && <p className="muted">No assignment data yet.</p>}
          </div>
        </article>

        <article className="panel-card">
          <header>
            <h3>Latest Collaboration</h3>
            <span>{overview.openWorkRate}% still in motion</span>
          </header>

          <div className="split-feed">
            <div className="list-stack">
              <strong className="section-kicker">Recent comments</strong>
              {overview.recentComments.map((comment) => (
                <div className="compact-card" key={comment.id}>
                  <strong>{comment.author?.name || "Unknown teammate"}</strong>
                  <small>{comment.text}</small>
                </div>
              ))}
              {!overview.recentComments.length && (
                <p className="muted">No comment activity yet.</p>
              )}
            </div>

            <div className="list-stack">
              <strong className="section-kicker">Recent files</strong>
              {overview.recentFiles.map((file) => (
                <div className="compact-card" key={file.id}>
                  <strong>{file.fileName}</strong>
                  <small>{file.mimeType}</small>
                </div>
              ))}
              {!overview.recentFiles.length && <p className="muted">No file activity yet.</p>}
            </div>
          </div>
        </article>
      </section>
    </section>
  );
}
