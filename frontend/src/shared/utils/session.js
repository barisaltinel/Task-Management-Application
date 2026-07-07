const SESSION_KEY = "taskpulse-session";

function readStorage(type) {
  try {
    return window[type];
  } catch {
    return null;
  }
}

function clearLegacySession() {
  const storage = readStorage("localStorage");
  storage?.removeItem(SESSION_KEY);
}

function normalizeUser(source = {}, fallbackEmail = "") {
  const email = source.email || fallbackEmail || "";
  const name =
    source.name ||
    [source.firstName, source.lastName].filter(Boolean).join(" ").trim() ||
    email;

  return {
    ...source,
    email,
    name
  };
}

function normalizeStoredSession(session) {
  if (!session || typeof session !== "object" || typeof session.token !== "string") {
    return null;
  }

  const token = session.token.trim();
  if (!token) {
    return null;
  }

  const userSource =
    session.user && typeof session.user === "object"
      ? session.user
      : { email: session.email, name: session.name };
  const user = normalizeUser(userSource, session.email);

  return {
    token,
    user,
    email: user.email,
    name: user.name
  };
}

export function createSession(authPayload, fallbackEmail = "") {
  const payload =
    authPayload?.data && typeof authPayload.data === "object" ? authPayload.data : authPayload;
  const token = [
    payload?.token,
    payload?.accessToken,
    payload?.bearerToken,
    payload?.access_token
  ].find((value) => typeof value === "string" && value.trim());

  if (!token) {
    throw new Error("Sign in succeeded but no access token was returned.");
  }

  const userSource =
    payload?.user && typeof payload.user === "object"
      ? payload.user
      : payload && typeof payload === "object"
        ? payload
        : {};
  const user = normalizeUser(userSource, fallbackEmail);

  return normalizeStoredSession({
    token,
    user
  });
}

export function readSession() {
  clearLegacySession();
  const storage = readStorage("sessionStorage");
  if (!storage) {
    return null;
  }

  try {
    const raw = storage.getItem(SESSION_KEY);
    const session = raw ? normalizeStoredSession(JSON.parse(raw)) : null;
    if (!session && raw) {
      storage.removeItem(SESSION_KEY);
    }
    return session;
  } catch {
    storage.removeItem(SESSION_KEY);
    return null;
  }
}

export function saveSession(session) {
  clearLegacySession();
  const storage = readStorage("sessionStorage");
  const normalizedSession = normalizeStoredSession(session);

  if (!storage || !normalizedSession) {
    storage?.removeItem(SESSION_KEY);
    return;
  }

  storage.setItem(SESSION_KEY, JSON.stringify(normalizedSession));
}

export function clearSession() {
  readStorage("sessionStorage")?.removeItem(SESSION_KEY);
  clearLegacySession();
}
