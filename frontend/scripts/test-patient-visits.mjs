import assert from "node:assert/strict";
import { buildPatientVisits } from "../src/services/patientVisits.js";

const appointments = [
  {
    id: "a1",
    serialNumber: "APT202603100001",
    department: "心内科",
    doctorName: "李医生",
    date: "2026-03-11",
    timeSlot: "09:00-09:30",
    status: "BOOKED",
    paymentStatus: "PAID",
    paidAt: "2026-03-10 15:39:30",
  },
];

const visitRecords = [
  {
    id: "visit-1001",
    appointmentId: "a1",
    department: "心内科",
    doctorName: "李敏",
    visitDate: "2026-03-10",
    visitTimeSlot: "09:00-09:30",
    status: "COMPLETED",
    diagnosis: "疑似心律失常",
    treatmentPlan: "建议完善心电图",
    doctorOrderNote: "低盐饮食",
    prescriptionNote: "阿司匹林口服",
    reportNote: "门诊复查",
  },
];

const visits = buildPatientVisits({ appointments, visitRecords, today: "2026-03-10" });

assert.equal(visits.length, 1);
assert.equal(visits[0].source, "医生接诊");
assert.equal(visits[0].status, "已完成");
assert.equal(visits[0].diagnosis, "疑似心律失常");
assert.equal(visits[0].visitNo, "VIS-1001");
assert.equal(visits[0].serialNumber, "APT202603100001");

const fallbackVisits = buildPatientVisits({ appointments, visitRecords: [], today: "2026-03-10" });
assert.equal(fallbackVisits.length, 1);
assert.equal(fallbackVisits[0].source, "预约挂号");
assert.equal(fallbackVisits[0].status, "已登记");
assert.equal(fallbackVisits[0].serialNumber, "APT202603100001");

console.log("patient visits mapping tests passed");