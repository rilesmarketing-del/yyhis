import { getCurrentUser } from "./auth.js";

export const DEFAULT_PATIENT = {
  id: "P1001",
  name: "张晓雪",
};

export const DEMO_PATIENTS = [DEFAULT_PATIENT];

const REGISTRATION_ONBOARDING_KEY = "hospital-demo-patient-registration-onboarding";

function readRegistrationOnboarding() {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const raw = window.sessionStorage.getItem(REGISTRATION_ONBOARDING_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
}

function writeRegistrationOnboarding(payload) {
  if (typeof window === "undefined") {
    return;
  }

  try {
    if (!payload) {
      window.sessionStorage.removeItem(REGISTRATION_ONBOARDING_KEY);
      return;
    }

    window.sessionStorage.setItem(REGISTRATION_ONBOARDING_KEY, JSON.stringify(payload));
  } catch (error) {
    // Ignore session storage failures so registration flow still works.
  }
}

export function setRegistrationOnboarding(payload) {
  if (!payload) {
    clearRegistrationOnboarding();
    return;
  }

  writeRegistrationOnboarding({
    username: payload.username || "",
    displayName: payload.displayName || DEFAULT_PATIENT.name,
    patientId: payload.patientId || DEFAULT_PATIENT.id,
  });
}

export function getRegistrationOnboarding() {
  return readRegistrationOnboarding();
}

export function clearRegistrationOnboarding() {
  writeRegistrationOnboarding(null);
}

export function getActivePatient() {
  const user = getCurrentUser();
  if (user?.role === "patient") {
    return {
      id: user.patientId || DEFAULT_PATIENT.id,
      name: user.displayName || DEFAULT_PATIENT.name,
    };
  }
  return DEFAULT_PATIENT;
}

export function setActivePatient() {
  return getActivePatient();
}

export function findPatientById(patientId) {
  const activePatient = getActivePatient();
  return activePatient.id === patientId ? activePatient : activePatient;
}