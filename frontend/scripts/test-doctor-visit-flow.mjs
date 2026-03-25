import assert from "node:assert/strict";
import fs from "node:fs";
import { pickCurrentVisit, sortDoctorPatients, sortDoctorQueue, sortDoctorRecords } from "../src/services/doctorClinic.js";

const queue = sortDoctorQueue([
  { appointmentId: "a2", date: "2026-03-10", timeSlot: "10:00-10:30", paidAt: "2026-03-10 09:10:00" },
  { appointmentId: "a1", date: "2026-03-10", timeSlot: "09:00-09:30", paidAt: "2026-03-10 08:50:00" },
]);
assert.equal(queue[0].appointmentId, "a1");

const records = sortDoctorRecords([
  { id: "v1", status: "COMPLETED", updatedAt: "2026-03-10 09:10:00" },
  { id: "v2", status: "IN_PROGRESS", updatedAt: "2026-03-10 09:00:00" },
]);
assert.equal(records[0].id, "v2");
assert.equal(pickCurrentVisit(records).id, "v2");

const patients = sortDoctorPatients([
  { patientId: "P2", latestVisitDate: "2026-03-09" },
  { patientId: "P1", latestVisitDate: "2026-03-10" },
]);
assert.equal(patients[0].patientId, "P1");

const doctorClinicView = fs.readFileSync(new URL("../src/views/doctor/DoctorClinic.vue", import.meta.url), "utf8");
assert.match(doctorClinicView, /empty-text="暂无待接诊数据"/);

console.log("doctor visit flow mapping tests passed");
