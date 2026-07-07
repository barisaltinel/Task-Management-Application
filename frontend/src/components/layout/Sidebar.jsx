import { VIEWS } from "../../app/config";
import { humanize } from "../../shared/utils/format";

export default function Sidebar({ email, view, setView, metrics }) {
  return (
    <aside className="side-panel reveal">
      <div>
        <div className="brand-mark">TaskPulse</div>
        <p className="side-text">{email}</p>
      </div>

      <nav className="side-nav">
        {VIEWS.map((item) => (
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
      </div>
    </aside>
  );
}
