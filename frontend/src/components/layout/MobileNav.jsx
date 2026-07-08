import { humanize } from "../../shared/utils/format";

export default function MobileNav({ view, setView, views }) {
  return (
    <div className="mobile-nav">
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
    </div>
  );
}
