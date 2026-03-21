export const LOGIN_MEMORY_STORAGE_KEY = "hospital-backoffice-login-memory";

const DEFAULT_BACKOFFICE_CREDENTIALS = {
  username: "admin",
  password: "admin123",
  rememberPassword: true,
};

function canUseStorage() {
  return typeof window !== "undefined" && Boolean(window.localStorage);
}

export function getRememberedBackofficeCredentials() {
  if (!canUseStorage()) {
    return { ...DEFAULT_BACKOFFICE_CREDENTIALS };
  }

  try {
    const raw = window.localStorage.getItem(LOGIN_MEMORY_STORAGE_KEY);
    if (!raw) {
      return { ...DEFAULT_BACKOFFICE_CREDENTIALS };
    }

    const parsed = JSON.parse(raw);
    if (!parsed?.username || !parsed?.password) {
      return { ...DEFAULT_BACKOFFICE_CREDENTIALS };
    }

    return {
      username: parsed.username,
      password: parsed.password,
      rememberPassword: parsed.rememberPassword !== false,
    };
  } catch (error) {
    return { ...DEFAULT_BACKOFFICE_CREDENTIALS };
  }
}

export function setRememberedBackofficeCredentials(credentials) {
  if (!canUseStorage()) {
    return false;
  }

  try {
    window.localStorage.setItem(
      LOGIN_MEMORY_STORAGE_KEY,
      JSON.stringify({
        username: credentials.username,
        password: credentials.password,
        rememberPassword: credentials.rememberPassword !== false,
      })
    );
    return true;
  } catch (error) {
    return false;
  }
}

export function clearRememberedBackofficeCredentials() {
  if (!canUseStorage()) {
    return false;
  }

  try {
    window.localStorage.removeItem(LOGIN_MEMORY_STORAGE_KEY);
    return true;
  } catch (error) {
    return false;
  }
}
