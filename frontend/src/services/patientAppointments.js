import { apiRequest } from "./apiClient";

export function fetchSchedules(params = {}) {
  const searchParams = new URLSearchParams();
  if (params.department) {
    searchParams.set("department", params.department);
  }
  if (params.date) {
    searchParams.set("date", params.date);
  }

  const suffix = searchParams.toString();
  return apiRequest(`/api/patient/appointments/schedules${suffix ? `?${suffix}` : ""}`);
}

export function createAppointment(payload) {
  return apiRequest("/api/patient/appointments", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function fetchMyAppointments() {
  return apiRequest("/api/patient/appointments/my");
}

export function payAppointment(appointmentId) {
  return apiRequest(`/api/patient/appointments/${appointmentId}/pay`, {
    method: "POST",
  });
}

export function rescheduleAppointment(appointmentId, payload) {
  return apiRequest(`/api/patient/appointments/${appointmentId}/reschedule`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function cancelAppointment(appointmentId) {
  return apiRequest(`/api/patient/appointments/${appointmentId}/cancel`, {
    method: "POST",
  });
}