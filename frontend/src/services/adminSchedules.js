import { apiRequest } from "./apiClient.js";

const TIGHT_REMAINING_THRESHOLD = 2;
const EMPTY_ADMIN_SCHEDULE_HINT = "当前还没有排班，请先新建排班。";

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

export function buildAdminScheduleInsights(schedules = [], now = new Date()) {
  const items = normalizeSchedules(schedules);
  const todayKey = formatDateKey(now);
  const enabledCount = items.filter((item) => item.enabled !== false).length;
  const todayCount = items.filter((item) => item.date === todayKey).length;
  const fullCount = items.filter((item) => item.enabled !== false && toSafeNumber(item.remainingSlots) <= 0).length;
  const tightCount = items.filter(
    (item) => item.enabled !== false && isTightRemainingCount(toSafeNumber(item.remainingSlots))
  ).length;

  return {
    summaryCards: [
      { label: "排班总数", value: items.length, hint: `${enabledCount} 条已启用` },
      { label: "今日排班", value: todayCount, hint: todayCount > 0 ? `按 ${todayKey} 统计` : "今天暂无排班" },
      { label: "紧张号源", value: tightCount, hint: `${fullCount} 条已约满` },
    ],
    emptyHint: EMPTY_ADMIN_SCHEDULE_HINT,
  };
}

export function getScheduleCapacityMeta(schedule = {}) {
  if (schedule.enabled === false) {
    return { label: "已停用", type: "info" };
  }

  const remainingSlots = toSafeNumber(schedule.remainingSlots);
  if (remainingSlots <= 0) {
    return { label: "已约满", type: "danger" };
  }
  if (isTightRemainingCount(remainingSlots)) {
    return { label: "紧张", type: "warning" };
  }
  return { label: "充足", type: "success" };
}

function normalizeSchedules(schedules) {
  return Array.isArray(schedules) ? schedules : [];
}

function isTightRemainingCount(remainingSlots) {
  return remainingSlots > 0 && remainingSlots <= TIGHT_REMAINING_THRESHOLD;
}

function toSafeNumber(value) {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : 0;
}

function formatDateKey(dateLike) {
  if (typeof dateLike === "string" && /^\d{4}-\d{2}-\d{2}$/.test(dateLike)) {
    return dateLike;
  }

  const date = dateLike instanceof Date ? dateLike : new Date(dateLike);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}