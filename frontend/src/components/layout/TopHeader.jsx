export default function TopHeader({ onRefresh, onLogout }) {
  return (
    <header className="top-header reveal">
      <div>
        <h2>Flow Center</h2>
        <p>Responsive workspace with live backend sync</p>
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
