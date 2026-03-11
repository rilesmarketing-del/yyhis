import { apiRequest } from "./apiClient.js";

const weekdayLabels = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

export function fetchMyDoctorSchedules() {
  return apiRequest("/api/doctor/schedule/mine");
}

export function buildDoctorScheduleModel(schedules = []) {
  const items = [...(Array.isArray(schedules) ? schedules : [])]
    .sort((left, right) => `${left.date || ""} ${left.timeSlot || ""}`.localeCompare(`${right.date || ""} ${right.timeSlot || ""}`))
    .map((item) => ({
      id: item.id,
      doctorUsername: item.doctorUsername || "",
      doctorName: item.doctorName || "",
      title: item.title || "",
      department: item.department || "",
      date: item.date || "",
      weekdayLabel: formatWeekday(item.date),
      timeSlot: item.timeSlot || "",
      feeLabel: `${Number(item.fee ?? 0).toFixed(2)} \u5143`,
      quotaLabel: `${Number(item.remainingSlots ?? 0)} / ${Number(item.totalSlots ?? 0)}`,
      statusLabel: item.enabled === false ? "\u5df2\u505c\u7528" : "\u5df2\u542f\u7528",
      statusType: item.enabled === false ? "info" : "success",
    }));

  return {
    items,
    emptyHint: "\u6682\u65e0\u6392\u73ed\u6570\u636e",
  };
}

function formatWeekday(date) {
  if (!date) {
    return "";
  }

  const parsed = new Date(`${date}T00:00:00`);
  if (Number.isNaN(parsed.getTime())) {
    return "";
  }

  return weekdayLabels[parsed.getDay()] || "";
}