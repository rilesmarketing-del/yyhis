import { clearAuthSession, getAuthToken } from "./auth.js";

export async function apiRequest(url, options = {}) {
  const requiresAuth = options.requiresAuth !== false;
  const headers = {
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(options.headers || {}),
  };

  if (requiresAuth) {
    const token = getAuthToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    if (response.status === 401 && requiresAuth) {
      clearAuthSession();
      if (typeof window !== "undefined" && !window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    }
    throw new Error(data?.message || "请求失败，请稍后重试");
  }

  return data;
}