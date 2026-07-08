import { humanize } from "../../shared/utils/format";

export default function Sidebar({ email, role, view, setView, views, metrics }) {
  return (
    <aside className="side-panel reveal">
      <div>
        <div className="brand-mark">TaskPulse</div>
        <p className="side-text">{email}</p>
        <p className="side-role">{humanize(role || "workspace")}</p>
      </div>

      <nav className="side-nav">
        {views.map((item) => (
          <button
            key={item}
            className={view === item ? "active" : ""}
            onClick={() => setView(item)}
            type="button"
          >
            {humanize(item)}
          </button>
        ))}
      </nav>

      <div className="quick-metrics">
        <div>
          <small>Tasks</small>
          <strong>{metrics.total}</strong>
        </div>
        <div>
          <small>Done</small>
          <strong>{metrics.done}</strong>
        </div>
        <div>
          <small>Active Projects</small>
          <strong>{metrics.activeProjects}</strong>
        </div>
      </div>
    </aside>
  );
}
