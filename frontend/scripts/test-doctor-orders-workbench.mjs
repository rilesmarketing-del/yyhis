import assert from "node:assert/strict";
import { pickCurrentVisit, buildDoctorOrdersDraft } from "../src/services/doctorClinic.js";

const records = [
  {
    id: "visit-1002",
    patientName: "王涵",
    status: "COMPLETED",
    doctorOrderNote: "已完成复诊评估。",
    prescriptionNote: "继续康复训练。",
    reportNote: "恢复情况良好。",
  },
  {
    id: "visit-1001",
    patientName: "张晓雪",
    status: "IN_PROGRESS",
    doctorOrderNote: "监测血压变化。",
    prescriptionNote: "阿司匹林肠溶片，每日一次。",
    reportNote: "复诊情况稳定。",
  },
];

const currentVisit = pickCurrentVisit(records);
assert.equal(currentVisit.id, "visit-1001");

const draft = buildDoctorOrdersDraft(currentVisit);
assert.deepEqual(draft, {
  doctorOrderNote: "监测血压变化。",
  prescriptionNote: "阿司匹林肠溶片，每日一次。",
  reportNote: "复诊情况稳定。",
});

const emptyDraft = buildDoctorOrdersDraft(null);
assert.deepEqual(emptyDraft, {
  doctorOrderNote: "",
  prescriptionNote: "",
  reportNote: "",
});

assert.equal(pickCurrentVisit(records.filter((item) => item.status !== "IN_PROGRESS")), null);

console.log("doctor orders workbench mapping tests passed");
