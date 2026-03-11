import { getCurrentUser } from "./auth.js";

export const DEFAULT_PATIENT = {
  id: "P1001",
  name: "张晓雪",
};

export const DEMO_PATIENTS = [DEFAULT_PATIENT];

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