export const SENIOR_CARE_MODE_STORAGE_KEY = "hospital-patient-senior-care-mode";

function canUseSessionStorage() {
  return typeof window !== "undefined" && Boolean(window.sessionStorage);
}

export function isSeniorCareModeEnabled() {
  if (!canUseSessionStorage()) {
    return false;
  }

  try {
    return window.sessionStorage.getItem(SENIOR_CARE_MODE_STORAGE_KEY) === "enabled";
  } catch (error) {
    return false;
  }
}

export function enableSeniorCareMode() {
  if (!canUseSessionStorage()) {
    return false;
  }

  try {
    window.sessionStorage.setItem(SENIOR_CARE_MODE_STORAGE_KEY, "enabled");
    return true;
  } catch (error) {
    return false;
  }
}

export function disableSeniorCareMode() {
  if (!canUseSessionStorage()) {
    return false;
  }

  try {
    window.sessionStorage.removeItem(SENIOR_CARE_MODE_STORAGE_KEY);
    return true;
  } catch (error) {
    return false;
  }
}
