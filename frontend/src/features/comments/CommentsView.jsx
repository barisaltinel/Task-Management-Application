export default function CommentsView({
  commentForm,
  setCommentForm,
  onCommentCreate,
  loading,
  tasks,
  comments,
}) {
  return (
    <section className="split-view">
      <article className="panel-card">
        <header>
          <h3>Add Comment</h3>
        </header>

        <form className="stack-form" onSubmit={onCommentCreate}>
          <label>
            Task
            <select
              className="modern-select"
              value={commentForm.taskId}
              onChange={(event) =>
                setCommentForm((prev) => ({ ...prev, taskId: event.target.value }))
              }
            >
              <option value="">Select task</option>
              {tasks.map((task) => (
                <option key={task.id} value={task.id}>
                  {task.title} - {task.project?.title || "No project"}
                </option>
              ))}
            </select>
          </label>

          <label>
            Comment
            <textarea
              rows={3}
              value={commentForm.text}
              onChange={(event) =>
                setCommentForm((prev) => ({ ...prev, text: event.target.value }))
              }
            />
          </label>

          <button className="primary-btn" disabled={loading} type="submit">
            Send
          </button>
        </form>
      </article>

      <article className="panel-card">
        <header>
          <h3>Comment Feed</h3>
          <span>{comments.length} messages</span>
        </header>

        <div className="list-stack">
          {comments.map((comment) => (
            <div className="list-row" key={comment.id}>
              <div>
                <strong>{comment.author?.name || "Unknown User"}</strong>
                <small>
                  {comment.text}
                  {comment.taskId ? ` | Task #${comment.taskId}` : ""}
                </small>
              </div>
              <span className="pill neutral">
                {new Date(comment.createdAt).toLocaleDateString()}
              </span>
            </div>
          ))}

          {!comments.length && <p className="muted">No comments.</p>}
        </div>
      </article>
    </section>
  );
}
