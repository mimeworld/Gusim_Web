import { attachmentError } from "./postOptions";
let csrf;
export async function api(path, options = {}) {
  const { silent = false, ...fetchOptions } = options;
  const method = fetchOptions.method || "GET";
  if (method !== "GET" && !csrf) {
    const response = await fetch("/api/csrf", { credentials: "same-origin" });
    if (!response.ok) throw new Error("서버에 연결할 수 없습니다.");
    csrf = await response.json();
  }
  const headers = {
    ...(options.body instanceof FormData
      ? {}
      : { "Content-Type": "application/json" }),
    ...options.headers,
  };
  if (method !== "GET") headers[csrf.headerName] = csrf.token;
  const response = await fetch("/api" + path, {
    ...fetchOptions,
    headers,
    credentials: "same-origin",
  });
  if (!response.ok) {
    if (response.status === 403) csrf = null;
    if (response.status === 401 && path !== "/auth/login")
      window.dispatchEvent(new Event("session-expired"));
    const payload = await response.json().catch(() => null);
    const message =
      payload?.message ||
      {
        401: "다시 로그인해주세요.",
        403: "권한이 없거나 세션이 만료되었습니다. 새로고침 후 다시 시도해주세요.",
        413: "파일 용량이 너무 큽니다.",
      }[response.status] ||
      "요청을 처리하지 못했습니다.";
    const error = new Error(message);
    error.status = response.status;
    if (!silent)
      window.dispatchEvent(new CustomEvent("api-error", { detail: message }));
    throw error;
  }
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}
export async function upload(file) {
  if (!file || file.size > 50 * 1024 * 1024) {
    const message = "50MB 이하 사진을 선택해주세요.";
    window.dispatchEvent(new CustomEvent("api-error", { detail: message }));
    throw new Error(message);
  }
  const body = new FormData();
  body.append("file", file);
  return (await api("/uploads", { method: "POST", body })).url;
}
export function resetCsrf() {
  csrf = null;
}

export async function uploadFiles(files, category) {
  const message = attachmentError(category, files);
  if (message) {
    window.dispatchEvent(new CustomEvent("api-error", { detail: message }));
    throw new Error(message);
  }
  if (!files.length) return [];
  const body = new FormData();
  body.append("category", category);
  files.forEach((file) => body.append("files", file));
  return (await api("/uploads/batch", { method: "POST", body })).urls;
}
