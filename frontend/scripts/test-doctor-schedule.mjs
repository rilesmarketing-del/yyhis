import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import {
  buildAdminScheduleInsights,
  createScheduleDraftFromDoctor,
  getScheduleCapacityMeta,
} from "../src/services/adminSchedules.js";
import { buildDoctorScheduleModel } from "../src/services/doctorSchedule.js";

const selectedDoctor = {
  username: "doctor_alpha",
  displayName: "Alpha Doctor",
  title: "Chief Physician",
  department: "Cardiology",
};

const draft = createScheduleDraftFromDoctor(selectedDoctor);
assert.equal(draft.doctorUsername, "doctor_alpha");
assert.equal(draft.doctorName, "Alpha Doctor");
assert.equal(draft.title, "Chief Physician");
assert.equal(draft.department, "Cardiology");

const adminSchedules = [
  {
    id: "SCH-TODAY-TIGHT",
    doctorUsername: "doctor_alpha",
    doctorName: "Alpha Doctor",
    title: "Chief Physician",
    department: "Cardiology",
    date: "2026-03-11",
    timeSlot: "09:00-09:30",
    fee: 28,
    totalSlots: 12,
    remainingSlots: 2,
    enabled: true,
  },
  {
    id: "SCH-TODAY-FULL",
    doctorUsername: "doctor_alpha",
    doctorName: "Alpha Doctor",
    title: "Chief Physician",
    department: "Cardiology",
    date: "2026-03-11",
    timeSlot: "13:00-13:30",
    fee: 28,
    totalSlots: 8,
    remainingSlots: 0,
    enabled: true,
  },
  {
    id: "SCH-WEEK-TIGHT",
    doctorUsername: "doctor_beta",
    doctorName: "Beta Doctor",
    title: "Attending Physician",
    department: "Neurology",
    date: "2026-03-14",
    timeSlot: "10:00-10:30",
    fee: 20,
    totalSlots: 6,
    remainingSlots: 1,
    enabled: true,
  },
  {
    id: "SCH-DISABLED",
    doctorUsername: "doctor_gamma",
    doctorName: "Gamma Doctor",
    title: "Resident",
    department: "Orthopedics",
    date: "2026-03-16",
    timeSlot: "15:00-15:30",
    fee: 16,
    totalSlots: 6,
    remainingSlots: 5,
    enabled: false,
  },
];

const adminInsights = buildAdminScheduleInsights(adminSchedules, new Date("2026-03-11T09:00:00"));
assert.deepEqual(
  adminInsights.summaryCards.map((item) => item.label),
  ["排班总数", "今日排班", "紧张号源"]
);
assert.equal(adminInsights.summaryCards[0].value, 4);
assert.equal(adminInsights.summaryCards[1].value, 2);
assert.equal(adminInsights.summaryCards[2].value, 2);
assert.equal(adminInsights.summaryCards[0].hint, "3 条已启用");
assert.equal(adminInsights.summaryCards[2].hint, "1 条已约满");
assert.equal(adminInsights.emptyHint, "当前还没有排班，请先新建排班。");
assert.deepEqual(getScheduleCapacityMeta(adminSchedules[0]), { label: "紧张", type: "warning" });
assert.deepEqual(getScheduleCapacityMeta(adminSchedules[1]), { label: "已约满", type: "danger" });
assert.deepEqual(getScheduleCapacityMeta(adminSchedules[3]), { label: "已停用", type: "info" });
assert.deepEqual(getScheduleCapacityMeta({ remainingSlots: 6, enabled: true }), { label: "充足", type: "success" });

const doctorModel = buildDoctorScheduleModel(
  [
    {
      id: "SCH-ALPHA",
      doctorUsername: "doctor_alpha",
      doctorName: "Alpha Doctor",
      title: "Chief Physician",
      department: "Cardiology",
      date: "2026-03-12",
      timeSlot: "09:00-09:30",
      fee: 28,
      totalSlots: 12,
      remainingSlots: 7,
      enabled: true,
    },
    {
      id: "SCH-BETA",
      doctorUsername: "doctor_alpha",
      doctorName: "Alpha Doctor",
      title: "Chief Physician",
      department: "Cardiology",
      date: "2026-03-15",
      timeSlot: "14:00-14:30",
      fee: 20,
      totalSlots: 8,
      remainingSlots: 1,
      enabled: true,
    },
    {
      id: "SCH-GAMMA",
      doctorUsername: "doctor_alpha",
      doctorName: "Alpha Doctor",
      title: "Chief Physician",
      department: "Cardiology",
      date: "2026-03-18",
      timeSlot: "10:00-10:30",
      fee: 22,
      totalSlots: 10,
      remainingSlots: 5,
      enabled: false,
    },
  ],
  new Date("2026-03-11T09:00:00")
);

assert.equal(doctorModel.items.length, 3);
assert.equal(doctorModel.items[0].weekdayLabel, "周四");
assert.equal(doctorModel.items[0].statusLabel, "已启用");
assert.equal(doctorModel.items[0].feeLabel, "28.00 元");
assert.equal(doctorModel.items[0].quotaLabel, "7 / 12");
assert.equal(doctorModel.summaryCards[0].label, "我的排班数");
assert.equal(doctorModel.summaryCards[0].value, 3);
assert.equal(doctorModel.summaryCards[1].label, "本周排班");
assert.equal(doctorModel.summaryCards[1].value, 2);
assert.equal(doctorModel.summaryCards[2].label, "最近一班");
assert.equal(doctorModel.summaryCards[2].value, "周四 09:00-09:30");
assert.match(doctorModel.summaryCards[2].hint, /2026-03-12/);
assert.equal(doctorModel.emptyHint, "当前医生账号下暂无排班，请联系管理端配置。");
assert.equal(
  buildDoctorScheduleModel([], new Date("2026-03-11T09:00:00")).emptyHint,
  "当前医生账号下暂无排班，请联系管理端配置。"
);

const [adminSchedulingView, doctorScheduleView] = await Promise.all([
  readFile(new URL("../src/views/admin/AdminScheduling.vue", import.meta.url), "utf8"),
  readFile(new URL("../src/views/doctor/DoctorSchedule.vue", import.meta.url), "utf8"),
]);

assert.match(adminSchedulingView, /scheduleInsights\.summaryCards/);
assert.match(adminSchedulingView, /scheduleInsights\.emptyHint/);
assert.match(adminSchedulingView, /getScheduleCapacityMeta/);
assert.doesNotMatch(adminSchedulingView, /:lg="16"/);
assert.match(doctorScheduleView, /scheduleModel\.summaryCards/);
assert.match(doctorScheduleView, /scheduleModel\.emptyHint/);

console.log("doctor schedule mapping tests passed");
