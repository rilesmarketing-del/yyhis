import assert from "node:assert/strict";
import { buildPatientPrescriptions } from "../src/services/patientPrescriptions.js";

const visitRecords = [
  {
    id: "visit-1001",
    department: "心内科",
    doctorName: "李敏",
    status: "COMPLETED",
    completedAt: "2026-03-09 11:20:00",
    prescriptionNote: "阿司匹林肠溶片，每日一次，每次一片。",
  },
  {
    id: "visit-1002",
    department: "呼吸内科",
    doctorName: "赵晴",
    status: "IN_PROGRESS",
    completedAt: null,
    prescriptionNote: "雾化吸入。",
  },
  {
    id: "visit-1003",
    department: "骨科",
    doctorName: "陈宇",
    status: "COMPLETED",
    completedAt: "2026-03-08 10:10:00",
    prescriptionNote: "",
  },
];

const prescriptions = buildPatientPrescriptions({ visitRecords });

assert.equal(prescriptions.length, 1);
assert.equal(prescriptions[0].presNo, "RX-1001");
assert.equal(prescriptions[0].dept, "心内科");
assert.equal(prescriptions[0].doctorName, "李敏");
assert.equal(prescriptions[0].status, "已开立");
assert.equal(prescriptions[0].content, "阿司匹林肠溶片，每日一次，每次一片。");

console.log("patient prescriptions mapping tests passed");