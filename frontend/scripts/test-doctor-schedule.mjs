import assert from "node:assert/strict";
import { createScheduleDraftFromDoctor } from "../src/services/adminSchedules.js";
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

const model = buildDoctorScheduleModel([
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
]);

assert.equal(model.items.length, 1);
assert.equal(model.items[0].statusLabel, "已启用");
assert.equal(model.items[0].feeLabel, "28.00 元");
assert.equal(model.items[0].quotaLabel, "7 / 12");
assert.equal(model.emptyHint, "暂无排班数据");

console.log("doctor schedule mapping tests passed");
