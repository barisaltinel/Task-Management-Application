export default function FilesView({
  uploadForm,
  setUploadForm,
  onUpload,
  loading,
  attachments
}) {
  return (
    <section className="split-view">
      <article className="panel-card">
        <header>
          <h3>Upload File</h3>
        </header>

        <form className="stack-form" onSubmit={onUpload}>
          <label>
            Task ID
            <input
              type="number"
              value={uploadForm.taskId}
              onChange={(event) =>
                setUploadForm((prev) => ({ ...prev, taskId: event.target.value }))
              }
            />
          </label>

          <label>
            File
            <input
              type="file"
              onChange={(event) =>
                setUploadForm((prev) => ({
                  ...prev,
                  file: event.target.files?.[0] || null
                }))
              }
            />
          </label>

          <button className="primary-btn" disabled={loading} type="submit">
            Upload
          </button>
        </form>
      </article>

      <article className="panel-card">
        <header>
          <h3>Attachments</h3>
          <span>{attachments.length} files</span>
        </header>

        <div className="list-stack">
          {attachments.map((file) => (
            <div className="list-row" key={file.id}>
              <div>
                <strong>{file.fileName}</strong>
                <small>
                  {file.mimeType} | {(file.fileSize / 1024).toFixed(1)} KB
                </small>
              </div>
            </div>
          ))}

          {!attachments.length && <p className="muted">No attachments.</p>}
        </div>
      </article>
    </section>
  );
}
