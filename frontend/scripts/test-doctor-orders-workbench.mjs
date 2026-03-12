import assert from "node:assert/strict";
import { pickCurrentVisit, buildDoctorOrdersDraft } from "../src/services/doctorClinic.js";

const records = [
  {
    id: "visit-1002",
    patientName: "Wang Han",
    status: "COMPLETED",
    doctorOrders: [
      { id: "ord-2", category: "CHECK", content: "Repeat blood pressure check", priority: "NORMAL" },
    ],
    prescriptions: [
      { id: "rx-2", drugName: "Amlodipine", dosage: "5mg", frequency: "Once daily", days: "14", remark: "Morning" },
    ],
    reports: [
      { id: "rep-2", itemName: "Blood Pressure", resultSummary: "Stable", resultFlag: "NORMAL", advice: "Keep monitoring" },
    ],
  },
  {
    id: "visit-1001",
    patientName: "Zhang Xiaoxue",
    status: "IN_PROGRESS",
    doctorOrders: [
      { id: "ord-1", category: "FOLLOW_UP", content: "Monitor blood pressure", priority: "IMPORTANT" },
    ],
    prescriptions: [
      { id: "rx-1", drugName: "Aspirin", dosage: "100mg", frequency: "Once daily", days: "7", remark: "After meals" },
    ],
    reports: [
      { id: "rep-1", itemName: "ECG", resultSummary: "Sinus rhythm", resultFlag: "ATTENTION", advice: "Review in 2 weeks" },
    ],
  },
];

const currentVisit = pickCurrentVisit(records);
assert.equal(currentVisit.id, "visit-1001");

const draft = buildDoctorOrdersDraft(currentVisit);
assert.deepEqual(draft, {
  doctorOrders: [
    { id: "ord-1", category: "FOLLOW_UP", content: "Monitor blood pressure", priority: "IMPORTANT" },
  ],
  prescriptions: [
    { id: "rx-1", drugName: "Aspirin", dosage: "100mg", frequency: "Once daily", days: "7", remark: "After meals" },
  ],
  reports: [
    { id: "rep-1", itemName: "ECG", resultSummary: "Sinus rhythm", resultFlag: "ATTENTION", advice: "Review in 2 weeks" },
  ],
});

const emptyDraft = buildDoctorOrdersDraft(null);
assert.deepEqual(emptyDraft, {
  doctorOrders: [],
  prescriptions: [],
  reports: [],
});

assert.equal(pickCurrentVisit(records.filter((item) => item.status !== "IN_PROGRESS")), null);

console.log("doctor orders workbench mapping tests passed");