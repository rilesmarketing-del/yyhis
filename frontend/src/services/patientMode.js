export const PATIENT_MODE_STORAGE_KEY = "hospital-frontdesk-patient-mode";

function readStoredPatientMode() {
  if (typeof window === "undefined") {
    return false;
  }

  try {
    return window.localStorage.getItem(PATIENT_MODE_STORAGE_KEY) === "enabled";
  } catch (error) {
    return false;
  }
}

export function isPatientModeEnabled() {
  return readStoredPatientMode();
}

export function enablePatientMode() {
  if (typeof window === "undefined") {
    return false;
  }

  try {
    window.localStorage.setItem(PATIENT_MODE_STORAGE_KEY, "enabled");
    return true;
  } catch (error) {
    return false;
  }
}

export function disablePatientMode() {
  if (typeof window === "undefined") {
    return false;
  }

  try {
    window.localStorage.removeItem(PATIENT_MODE_STORAGE_KEY);
    return true;
  } catch (error) {
    return false;
  }
}

export function isRoleBlockedInPatientMode(role) {
  return isPatientModeEnabled() && ["doctor", "admin"].includes(role || "");
}
