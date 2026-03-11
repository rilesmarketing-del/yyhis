import assert from "node:assert/strict";
import { buildPatientReports } from "../src/services/patientReports.js";

const appointments = [
  {
    id: "a1",
    serialNumber: "APT202603100001",
    department: "心内科",
    doctorName: "李敏",
    date: "2026-03-09",
    timeSlot: "09:00-09:30",
    status: "BOOKED",
    paymentStatus: "PAID",
    paidAt: "2026-03-10 15:39:30",
  },
  {
    id: "a2",
    serialNumber: "APT202603100002",
    department: "骨科",
    doctorName: "王涵",
    date: "2026-03-10",
    timeSlot: "14:00-14:30",
    status: "BOOKED",
    paymentStatus: "PAID",
    paidAt: "2026-03-10 11:00:00",
  },
];

const visitRecords = [
  {
    id: "visit-1001",
    appointmentId: "a1",
    department: "心内科",
    doctorName: "李敏",
    visitDate: "2026-03-09",
    visitTimeSlot: "09:00-09:30",
    status: "COMPLETED",
    reportNote: "复诊情况稳定，建议一月后复查。",
    treatmentPlan: "继续低盐饮食，按时复诊。",
    completedAt: "2026-03-09 11:20:00",
  },
];

const reports = buildPatientReports({ appointments, visitRecords, today: "2026-03-10" });

assert.equal(reports.length, 1);
assert.equal(reports[0].source, "医生接诊");
assert.equal(reports[0].reportNo, "REP-1001");
assert.equal(reports[0].summary, "复诊情况稳定，建议一月后复查。");
assert.equal(reports[0].advice, "继续低盐饮食，按时复诊。");
assert.equal(reports[0].serialNumber, "a1");

const fallbackReports = buildPatientReports({ appointments, visitRecords: [], today: "2026-03-10" });
assert.equal(fallbackReports.length, 2);
assert.equal(fallbackReports[0].source, "预约映射");

console.log("patient reports mapping tests passed");