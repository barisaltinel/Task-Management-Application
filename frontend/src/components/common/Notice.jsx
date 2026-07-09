const TITLES = {
  success: "Success",
  error: "Attention",
  info: "Notice",
};

export default function Notice({ notice, onDismiss }) {
  if (!notice?.text) return null;

  const type = notice.type || "info";
  const title = notice.title || TITLES[type] || TITLES.info;

  return (
    <div
      className={`notice ${type}`}
      role={type === "error" ? "alert" : "status"}
      aria-live={type === "error" ? "assertive" : "polite"}
    >
      <div className="notice__copy">
        <strong>{title}</strong>
        <span>{notice.text}</span>
      </div>

      {onDismiss && (
        <button
          type="button"
          className="notice__dismiss"
          onClick={onDismiss}
          aria-label="Dismiss notice"
        >
          Close
        </button>
      )}
    </div>
  );
}
