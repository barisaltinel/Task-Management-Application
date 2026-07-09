import Notice from "../../components/common/Notice";

export default function AuthView({
  authMode,
  setAuthMode,
  loginForm,
  setLoginForm,
  registerForm,
  setRegisterForm,
  loading,
  onLogin,
  onRegister,
  notice,
  onDismissNotice,
}) {
  const isLogin = authMode === "login";

  return (
    <div className="auth-shell">
      <div className="auth-card reveal">
        <section className="auth-showcase">
          <div className="brand-mark">TaskPulse</div>
          <p className="auth-eyebrow">Team work, without the rough edges</p>
          <h1>Start your workspace with a calmer first step.</h1>
          <p className="auth-lead">
            Bring tasks, project status, files, and discussion into one focused flow from the moment
            your team signs in.
          </p>

          <div className="auth-highlights" aria-label="TaskPulse highlights">
            <div>
              <strong>Clear boards</strong>
              <span>Track progress from backlog to completion without noise.</span>
            </div>
            <div>
              <strong>Shared context</strong>
              <span>Keep files, comments, and delivery details attached to the work.</span>
            </div>
            <div>
              <strong>Safer sessions</strong>
              <span>Your sign-in stays in this browser tab, not across the whole device.</span>
            </div>
          </div>
        </section>

        <section className="auth-panel">
          <div className="auth-panel__header">
            <div>
              <h2>{isLogin ? "Welcome back" : "Create your account"}</h2>
              <p>
                {isLogin
                  ? "Sign in to open your workspace and sync the latest activity."
                  : "Set up access for this workspace and get your team moving quickly."}
              </p>
            </div>

            <div className="auth-toggle">
              <button
                type="button"
                className={isLogin ? "active" : ""}
                aria-pressed={isLogin}
                onClick={() => {
                  onDismissNotice?.();
                  setAuthMode("login");
                }}
              >
                Sign In
              </button>
              <button
                type="button"
                className={!isLogin ? "active" : ""}
                aria-pressed={!isLogin}
                onClick={() => {
                  onDismissNotice?.();
                  setAuthMode("register");
                }}
              >
                Register
              </button>
            </div>
          </div>

          <form className="stack-form" onSubmit={isLogin ? onLogin : onRegister}>
            {!isLogin && (
              <label>
                Name
                <input
                  autoComplete="name"
                  placeholder="Alex Morgan"
                  value={registerForm.name}
                  onChange={(event) =>
                    setRegisterForm((prev) => ({ ...prev, name: event.target.value }))
                  }
                />
              </label>
            )}

            <label>
              Email
              <input
                type="email"
                autoComplete="email"
                placeholder="name@company.com"
                value={isLogin ? loginForm.email : registerForm.email}
                onChange={(event) =>
                  isLogin
                    ? setLoginForm((prev) => ({ ...prev, email: event.target.value }))
                    : setRegisterForm((prev) => ({ ...prev, email: event.target.value }))
                }
              />
            </label>

            <label>
              Password
              <input
                type="password"
                autoComplete={isLogin ? "current-password" : "new-password"}
                placeholder={isLogin ? "Enter your password" : "Create a secure password"}
                value={isLogin ? loginForm.password : registerForm.password}
                onChange={(event) =>
                  isLogin
                    ? setLoginForm((prev) => ({ ...prev, password: event.target.value }))
                    : setRegisterForm((prev) => ({ ...prev, password: event.target.value }))
                }
              />
            </label>

            <button className="primary-btn" disabled={loading} type="submit">
              {loading ? "Please wait..." : isLogin ? "Open Workspace" : "Create Account"}
            </button>
          </form>

          <p className="auth-footnote">
            {isLogin
              ? "Your session is kept for this tab only, which helps shared devices stay tidy."
              : "You can sign in right after registration and land directly in the workspace."}
          </p>

          <Notice notice={notice} onDismiss={onDismissNotice} />
        </section>
      </div>
    </div>
  );
}
