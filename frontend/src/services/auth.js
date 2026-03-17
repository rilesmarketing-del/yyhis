import { reactive } from "vue";

const STORAGE_KEY = "hospital-demo-auth-session";

export const authState = reactive({
  token: "",
  currentUser: null,
});

let resolvingCurrentUser = null;

function readStoredSession() {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
}

function persistSession() {
  if (typeof window === "undefined") {
    return;
  }

  if (!authState.token) {
    window.localStorage.removeItem(STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: authState.token,
      currentUser: authState.currentUser,
    })
  );
}

function applySession(session) {
  authState.token = session?.token || "";
  authState.currentUser = session?.currentUser || null;
  persistSession();
}

async function rawRequest(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(data?.message || "请求失败，请稍后重试");
  }

  return data;
}

const storedSession = readStoredSession();
if (storedSession?.token) {
  authState.token = storedSession.token;
  authState.currentUser = storedSession.currentUser || null;
}

export function getAuthToken() {
  return authState.token;
}

export function getCurrentUser() {
  return authState.currentUser;
}

export function clearAuthSession() {
  applySession(null);
}

export function setAuthSession(session) {
  applySession(session);
}

export function isAuthenticated() {
  return Boolean(authState.token && authState.currentUser);
}

export async function login(credentials) {
  const response = await rawRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });

  applySession({
    token: response.token,
    currentUser: {
      username: response.username,
      role: response.role,
      displayName: response.displayName,
      patientId: response.patientId,
    },
  });

  return authState.currentUser;
}

export async function loginForVerification(credentials) {
  const response = await rawRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });

  return {
    token: response.token,
    currentUser: {
      username: response.username,
      role: response.role,
      displayName: response.displayName,
      patientId: response.patientId,
    },
  };
}

export function registerPatient(payload) {
  return rawRequest("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function ensureCurrentUser() {
  if (authState.token && authState.currentUser) {
    return authState.currentUser;
  }
  if (!authState.token) {
    return null;
  }
  if (resolvingCurrentUser) {
    return resolvingCurrentUser;
  }

  resolvingCurrentUser = rawRequest("/api/auth/me", {
    headers: {
      Authorization: `Bearer ${authState.token}`,
    },
  })
    .then((response) => {
      applySession({
        token: authState.token,
        currentUser: {
          username: response.username,
          role: response.role,
          displayName: response.displayName,
          patientId: response.patientId,
        },
      });
      return authState.currentUser;
    })
    .catch(() => {
      clearAuthSession();
      return null;
    })
    .finally(() => {
      resolvingCurrentUser = null;
    });

  return resolvingCurrentUser;
}

export async function logout() {
  const token = authState.token;
  try {
    if (token) {
      await rawRequest("/api/auth/logout", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
    }
  } finally {
    clearAuthSession();
  }
}
