import { VIEWS } from "../../app/config";
import { humanize } from "../../shared/utils/format";

export default function MobileNav({ view, setView }) {
  return (
    <div className="mobile-nav">
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
    </div>
  );
}
