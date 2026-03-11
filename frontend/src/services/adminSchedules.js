import { apiRequest } from "./apiClient.js";

export function fetchAdminSchedules() {
  return apiRequest("/api/admin/schedules");
}

export function fetchScheduleDoctorOptions() {
  return apiRequest("/api/admin/schedules/doctors");
}

export function createAdminSchedule(payload) {
  return apiRequest("/api/admin/schedules", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateAdminSchedule(scheduleId, payload) {
  return apiRequest(`/api/admin/schedules/${scheduleId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function disableAdminSchedule(scheduleId) {
  return apiRequest(`/api/admin/schedules/${scheduleId}/disable`, {
    method: "POST",
  });
}

export function createScheduleDraftFromDoctor(doctor = {}) {
  return {
    doctorUsername: doctor.username || "",
    doctorName: doctor.displayName || "",
    title: doctor.title || "",
    department: doctor.department || "",
  };
}