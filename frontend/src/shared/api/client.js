const API_BASE = import.meta.env.VITE_API_BASE_URL || "/api";

export class ApiError extends Error {
  constructor(message, { status, payload } = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

function parseResponse(response) {
  const contentType = response.headers.get("content-type") || "";
  if (response.status === 204) {
    return Promise.resolve(null);
  }
  if (contentType.includes("application/json")) {
    return response.json();
  }
  return response.text();
}

function normalizeErrorPayload(payload) {
  if (!payload) return "Unexpected error";
  if (typeof payload === "string") return payload;
  if (Array.isArray(payload)) return payload.join(" | ");
  if (typeof payload === "object") {
    if (typeof payload.message === "string") return payload.message;
    if (typeof payload.error === "string") return payload.error;
    if (typeof payload.detail === "string") return payload.detail;
    return Object.values(payload)
      .flatMap((value) => (Array.isArray(value) ? value : [value]))
      .join(" | ");
  }
  return "Unexpected error";
}

export async function apiRequest(
  path,
  { method = "GET", token = "", body = null, isFormData = false } = {}
) {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (body && !isFormData) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body
      ? isFormData
        ? body
        : JSON.stringify(body)
      : null
  });

  const payload = await parseResponse(response);
  if (!response.ok) {
    throw new ApiError(normalizeErrorPayload(payload), {
      status: response.status,
      payload
    });
  }

  return payload;
}
