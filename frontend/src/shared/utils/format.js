export function humanize(value) {
  return value.toLowerCase().replaceAll("_", " ");
}

export function formatDateLabel(value) {
  if (!value) {
    return "No date";
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric"
  }).format(new Date(`${value}T00:00:00`));
}

export function formatRelativeDeadline(value) {
  if (!value) {
    return "No deadline";
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const dueDate = new Date(`${value}T00:00:00`);
  const diffInDays = Math.round((dueDate.getTime() - today.getTime()) / 86400000);

  if (diffInDays < 0) {
    return `${Math.abs(diffInDays)} day${Math.abs(diffInDays) === 1 ? "" : "s"} overdue`;
  }
  if (diffInDays === 0) {
    return "Due today";
  }
  if (diffInDays === 1) {
    return "Due tomorrow";
  }

  return `Due in ${diffInDays} days`;
}
