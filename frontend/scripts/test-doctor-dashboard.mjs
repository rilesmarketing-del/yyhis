import assert from "node:assert/strict";
import { buildDoctorDashboardModel } from "../src/services/doctorDashboard.js";

const queue = [
  {
    appointmentId: "a1",
    patientName: "张晓雪",
    department: "心内科",
    date: "2026-03-10",
    timeSlot: "09:00-09:30",
    paidAt: "2026-03-10 08:40:00",
  },
  {
    appointmentId: "a2",
    patientName: "王涵",
    department: "心内科",
    date: "2026-03-10",
    timeSlot: "10:00-10:30",
    paidAt: "2026-03-10 09:15:00",
  },
];

const records = [
  {
    id: "visit-1001",
    patientName: "李芳",
    department: "心内科",
    visitDate: "2026-03-10",
    visitTimeSlot: "08:30-09:00",
    status: "COMPLETED",
  },
  {
    id: "visit-1002",
    patientName: "陈宇",
    department: "心内科",
    visitDate: "2026-03-10",
    visitTimeSlot: "09:30-10:00",
    status: "IN_PROGRESS",
  },
  {
    id: "visit-1003",
    patientName: "赵晴",
    department: "心内科",
    visitDate: "2026-03-09",
    visitTimeSlot: "15:00-15:30",
    status: "COMPLETED",
  },
];

const patients = [
  { patientId: "P1001", patientName: "张晓雪" },
  { patientId: "P1002", patientName: "王涵" },
  { patientId: "P1003", patientName: "李芳" },
];

const model = buildDoctorDashboardModel({ queue, records, patients, today: "2026-03-10" });

assert.equal(model.stats[0].value, "2");
assert.equal(model.stats[1].value, "2");
assert.equal(model.stats[2].value, "1");
assert.equal(model.stats[3].value, "3");
assert.equal(model.overview.length, 4);
assert.equal(model.overview[0].status, "待接诊");
assert.equal(model.overview[2].status, "接诊中");
assert.equal(model.overview[3].status, "已完成");
assert.equal(model.todos[0].text, "当前有 2 位患者待接诊");
assert.equal(model.todos[1].text, "有 1 份病历待完成");
assert.equal(model.todos[2].text, "今日已完成 1 次接诊");

console.log("doctor dashboard mapping tests passed");
