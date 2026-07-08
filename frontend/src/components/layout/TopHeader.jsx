import { humanize } from "../../shared/utils/format";

export default function TopHeader({ role, overview, onRefresh, onLogout }) {
  return (
    <header className="top-header reveal">
      <div>
        <h2>Flow Center</h2>
        <p>
          {humanize(role || "workspace")} workspace with live backend sync,{" "}
          {overview.completionRate}% completion, and {overview.atRiskCount} active risk signal
          {overview.atRiskCount === 1 ? "" : "s"}.
        </p>
      </div>
      <div className="header-actions">
        <button className="ghost-btn" onClick={onRefresh} type="button">
          Refresh
        </button>
        <button className="ghost-btn danger" onClick={onLogout} type="button">
          Logout
        </button>
      </div>
    </header>
  );
}
