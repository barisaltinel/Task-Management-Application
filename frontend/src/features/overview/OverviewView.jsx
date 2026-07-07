export default function OverviewView({ metrics }) {
  return (
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
  );
}
