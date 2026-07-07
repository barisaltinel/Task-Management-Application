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
  notice
}) {
  const isLogin = authMode === "login";

  return (
    <div className="auth-shell">
      <div className="auth-card reveal">
        <div className="brand-mark">TaskPulse</div>
        <h1>Modern task flow for fast teams</h1>

        <div className="auth-toggle">
          <button
            type="button"
            className={isLogin ? "active" : ""}
            onClick={() => setAuthMode("login")}
          >
            Sign In
          </button>
          <button
            type="button"
            className={!isLogin ? "active" : ""}
            onClick={() => setAuthMode("register")}
          >
            Register
          </button>
        </div>

        <form className="stack-form" onSubmit={isLogin ? onLogin : onRegister}>
          {!isLogin && (
            <label>
              Name
              <input
                autoComplete="name"
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
              value={isLogin ? loginForm.password : registerForm.password}
              onChange={(event) =>
                isLogin
                  ? setLoginForm((prev) => ({ ...prev, password: event.target.value }))
                  : setRegisterForm((prev) => ({ ...prev, password: event.target.value }))
              }
            />
          </label>

          <button className="primary-btn" disabled={loading} type="submit">
            {loading ? "Please wait..." : isLogin ? "Sign In" : "Create Account"}
          </button>
        </form>

        <Notice notice={notice} />
      </div>
    </div>
  );
}
