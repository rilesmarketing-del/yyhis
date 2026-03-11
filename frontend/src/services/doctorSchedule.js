import { apiRequest } from "./apiClient.js";

const weekdayLabels = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
const EMPTY_DOCTOR_SCHEDULE_HINT = "当前医生账号下暂无排班，请联系管理端配置。";

export function fetchMyDoctorSchedules() {
  return apiRequest("/api/doctor/schedule/mine");
}

export function buildDoctorScheduleModel(schedules = [], now = new Date()) {
  const normalizedSchedules = [...(Array.isArray(schedules) ? schedules : [])].sort((left, right) =>
    buildScheduleSortKey(left).localeCompare(buildScheduleSortKey(right))
  );
  const enabledSchedules = normalizedSchedules.filter((item) => item.enabled !== false);
  const weekRange = getWeekRange(now);
  const weekCount = enabledSchedules.filter((item) => item.date >= weekRange.start && item.date <= weekRange.end).length;
  const upcomingSchedule = enabledSchedules.find((item) => isUpcomingSchedule(item, now)) || enabledSchedules[0] || null;

  const items = normalizedSchedules.map((item) => ({
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
    summaryCards: [
      { label: "我的排班数", value: normalizedSchedules.length, hint: `${enabledSchedules.length} 条已启用` },
      { label: "本周排班", value: weekCount, hint: `${weekRange.start} 至 ${weekRange.end}` },
      {
        label: "最近一班",
        value: upcomingSchedule ? `${formatWeekday(upcomingSchedule.date)} ${upcomingSchedule.timeSlot}` : "暂无安排",
        hint: upcomingSchedule
          ? `${upcomingSchedule.date} ${upcomingSchedule.department} / ${upcomingSchedule.title}`
          : "请联系管理端配置",
      },
    ],
    emptyHint: EMPTY_DOCTOR_SCHEDULE_HINT,
  };
}

function buildScheduleSortKey(schedule = {}) {
  return `${schedule.date || ""} ${extractSlotStart(schedule.timeSlot) || schedule.timeSlot || ""}`;
}

function isUpcomingSchedule(schedule = {}, now = new Date()) {
  const scheduleKey = buildScheduleSortKey(schedule);
  const currentKey = `${formatDateKey(now)} ${formatTimeKey(now)}`;
  return scheduleKey >= currentKey;
}

function getWeekRange(now = new Date()) {
  const currentDate = toDate(now);
  const weekday = currentDate.getDay();
  const diffToMonday = weekday === 0 ? -6 : 1 - weekday;
  const start = addDays(currentDate, diffToMonday);
  const end = addDays(start, 6);

  return {
    start: formatDateKey(start),
    end: formatDateKey(end),
  };
}

function addDays(date, offset) {
  const nextDate = new Date(date.getTime());
  nextDate.setDate(nextDate.getDate() + offset);
  return nextDate;
}

function formatWeekday(date) {
  if (!date) {
    return "";
  }

  const parsed = toDate(date);
  if (Number.isNaN(parsed.getTime())) {
    return "";
  }

  return weekdayLabels[parsed.getDay()] || "";
}

function formatDateKey(dateLike) {
  const date = toDate(dateLike);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatTimeKey(dateLike) {
  const date = toDate(dateLike);
  if (Number.isNaN(date.getTime())) {
    return "00:00";
  }

  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${hours}:${minutes}`;
}

function extractSlotStart(timeSlot = "") {
  const [start] = String(timeSlot).split("-");
  return start || "";
}

function toDate(dateLike) {
  if (typeof dateLike === "string" && /^\d{4}-\d{2}-\d{2}$/.test(dateLike)) {
    return new Date(`${dateLike}T00:00:00`);
  }
  return dateLike instanceof Date ? dateLike : new Date(dateLike);
}