import { apiRequest } from "./apiClient.js";

export const visitStatusMeta = {
  IN_PROGRESS: { label: "接诊中", type: "warning" },
  COMPLETED: { label: "已完成", type: "success" },
};

export function fetchDoctorQueue() {
  return apiRequest("/api/doctor/clinic/queue");
}

export function startDoctorVisit(appointmentId) {
  return apiRequest(`/api/doctor/clinic/${appointmentId}/start`, {
    method: "POST",
  });
}

export function fetchDoctorRecords() {
  return apiRequest("/api/doctor/records");
}

export function fetchDoctorRecord(visitId) {
  return apiRequest(`/api/doctor/records/${visitId}`);
}

export function saveDoctorRecord(visitId, payload) {
  return apiRequest(`/api/doctor/records/${visitId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function completeDoctorRecord(visitId) {
  return apiRequest(`/api/doctor/records/${visitId}/complete`, {
    method: "POST",
  });
}

export function fetchDoctorPatients() {
  return apiRequest("/api/doctor/patients");
}

export function fetchPatientVisits() {
  return apiRequest("/api/patient/visits");
}

export function sortDoctorQueue(queue = []) {
  return [...queue].sort((left, right) => {
    const leftKey = `${left.date || ""} ${left.timeSlot || ""} ${left.paidAt || ""}`;
    const rightKey = `${right.date || ""} ${right.timeSlot || ""} ${right.paidAt || ""}`;
    return leftKey.localeCompare(rightKey);
  });
}

export function sortDoctorRecords(records = []) {
  return [...records].sort((left, right) => {
    if (left.status !== right.status) {
      return left.status === "IN_PROGRESS" ? -1 : 1;
    }
    return String(right.updatedAt || "").localeCompare(String(left.updatedAt || ""));
  });
}

export function pickCurrentVisit(records = []) {
  return sortDoctorRecords(records).find((item) => item.status === "IN_PROGRESS") || null;
}

export function buildDoctorOrdersDraft(record) {
  return {
    doctorOrderNote: record?.doctorOrderNote || "",
    prescriptionNote: record?.prescriptionNote || "",
    reportNote: record?.reportNote || "",
  };
}

export function sortDoctorPatients(patients = []) {
  return [...patients].sort((left, right) => String(right.latestVisitDate || "").localeCompare(String(left.latestVisitDate || "")));
}
