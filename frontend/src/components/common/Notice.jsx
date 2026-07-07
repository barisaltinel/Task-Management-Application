export default function Notice({ notice }) {
  if (!notice?.text) return null;
  return <div className={`notice ${notice.type}`}>{notice.text}</div>;
}
