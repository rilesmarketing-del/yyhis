import { apiRequest } from "./apiClient.js";

const weekdayLabels = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];

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
      feeLabel: `${Number(item.fee ?? 0).toFixed(2)} 元`,
      quotaLabel: `${Number(item.remainingSlots ?? 0)} / ${Number(item.totalSlots ?? 0)}`,
      statusLabel: item.enabled === false ? "已停用" : "已启用",
      statusType: item.enabled === false ? "info" : "success",
    }));

  return {
    items,
    emptyHint: "暂无排班数据",
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