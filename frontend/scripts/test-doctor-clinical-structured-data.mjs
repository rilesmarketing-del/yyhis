import assert from "node:assert/strict";
import { buildDoctorOrdersDraft } from "../src/services/doctorClinic.js";
import { buildPatientReports } from "../src/services/patientReports.js";
import { buildPatientPrescriptions } from "../src/services/patientPrescriptions.js";
import { buildAdminPharmacyModel } from "../src/services/adminPharmacy.js";

const draft = buildDoctorOrdersDraft({
  doctorOrders: [
    { id: "ord-1", category: "FOLLOW_UP", content: "Revisit in one week", priority: "IMPORTANT" },
  ],
  prescriptions: [
    { id: "rx-1", drugName: "Aspirin", dosage: "100mg", frequency: "Once daily", days: "7", remark: "After meals" },
  ],
  reports: [
    { id: "rep-1", itemName: "ECG", resultSummary: "Sinus rhythm", resultFlag: "ATTENTION", advice: "Review in two weeks" },
  ],
});

assert.equal(Array.isArray(draft.doctorOrders), true);
assert.equal(Array.isArray(draft.prescriptions), true);
assert.equal(Array.isArray(draft.reports), true);
assert.equal(draft.doctorOrders.length, 1);
assert.equal(draft.prescriptions.length, 1);
assert.equal(draft.reports.length, 1);

const reports = buildPatientReports({
  appointments: [],
  visitRecords: [
    {
      id: "visit-1001",
      appointmentId: "APT-1",
      department: "Cardiology",
      doctorName: "Dr. Li",
      visitDate: "2026-03-12",
      visitTimeSlot: "09:00-09:30",
      status: "COMPLETED",
      treatmentPlan: "Continue follow-up",
      completedAt: "2026-03-12 10:00:00",
      reportNote: "",
      reports: [
        { id: "rep-1", itemName: "ECG", resultSummary: "Sinus rhythm", resultFlag: "ATTENTION", advice: "Review in two weeks" },
      ],
    },
  ],
  today: "2026-03-12",
});

assert.equal(reports.length, 1);
assert.equal(reports[0].item, "ECG");
assert.equal(reports[0].summary, "Sinus rhythm");
assert.equal(reports[0].result, "ATTENTION");
assert.equal(reports[0].advice, "Review in two weeks");

const prescriptions = buildPatientPrescriptions({
  visitRecords: [
    {
      id: "visit-1001",
      appointmentId: "APT-1",
      department: "Cardiology",
      doctorName: "Dr. Li",
      status: "COMPLETED",
      completedAt: "2026-03-12 10:00:00",
      prescriptionNote: "",
      prescriptions: [
        { id: "rx-1", drugName: "Aspirin", dosage: "100mg", frequency: "Once daily", days: "7", remark: "After meals" },
      ],
    },
  ],
});

assert.equal(prescriptions.length, 1);
assert.equal(Array.isArray(prescriptions[0].items), true);
assert.equal(prescriptions[0].items.length, 1);
assert.equal(prescriptions[0].items[0].drugName, "Aspirin");
assert.match(prescriptions[0].content, /Aspirin/);

const pharmacyModel = buildAdminPharmacyModel({
  cards: {
    totalPrescriptions: 1,
    todayPrescriptions: 1,
    patientCount: 1,
  },
  records: [
    {
      id: "VISIT-1001",
      patientId: "P1001",
      patientName: "Alice",
      department: "Cardiology",
      doctorName: "Dr. Li",
      visitDate: "2026-03-12",
      visitTimeSlot: "09:00-09:30",
      status: "COMPLETED",
      prescriptionNote: "",
      prescriptions: [
        { id: "rx-1", drugName: "Aspirin", dosage: "100mg", frequency: "Once daily", days: "7", remark: "After meals" },
      ],
    },
  ],
});

assert.equal(pharmacyModel.records.length, 1);
assert.equal(pharmacyModel.records[0].structuredCount, 1);
assert.match(pharmacyModel.records[0].prescriptionPreview, /Aspirin/);

console.log("doctor clinical structured data mapping tests passed");