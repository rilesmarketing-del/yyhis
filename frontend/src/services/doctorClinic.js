import { apiRequest } from "./apiClient.js";

export const visitStatusMeta = {
  IN_PROGRESS: { label: "接诊中", type: "warning" },
  COMPLETED: { label: "已完成", type: "success" },
};

export const doctorOrderCategoryOptions = [
  { label: "生活方式", value: "LIFESTYLE" },
  { label: "用药指导", value: "MEDICATION" },
  { label: "复诊安排", value: "FOLLOW_UP" },
  { label: "检查建议", value: "CHECK" },
];

export const doctorOrderPriorityOptions = [
  { label: "常规", value: "NORMAL" },
  { label: "重点", value: "IMPORTANT" },
];

export const reportFlagOptions = [
  { label: "正常", value: "NORMAL" },
  { label: "关注", value: "ATTENTION" },
  { label: "需复查", value: "FOLLOW_UP" },
];

function trimText(value) {
  return String(value || "").trim();
}

function nextDraftId(prefix) {
  return `${prefix}-${Math.random().toString(36).slice(2, 10)}`;
}

export function createDoctorOrderItem() {
  return {
    id: nextDraftId("ord"),
    category: "FOLLOW_UP",
    content: "",
    priority: "NORMAL",
  };
}

export function createPrescriptionItem() {
  return {
    id: nextDraftId("rx"),
    drugName: "",
    dosage: "",
    frequency: "",
    days: "",
    remark: "",
  };
}

export function createReportItem() {
  return {
    id: nextDraftId("rep"),
    itemName: "",
    resultSummary: "",
    resultFlag: "NORMAL",
    advice: "",
  };
}

function cloneDoctorOrderItem(item = {}) {
  return {
    id: trimText(item.id) || nextDraftId("ord"),
    category: trimText(item.category) || "FOLLOW_UP",
    content: trimText(item.content),
    priority: trimText(item.priority) || "NORMAL",
  };
}

function clonePrescriptionItem(item = {}) {
  return {
    id: trimText(item.id) || nextDraftId("rx"),
    drugName: trimText(item.drugName),
    dosage: trimText(item.dosage),
    frequency: trimText(item.frequency),
    days: trimText(item.days),
    remark: trimText(item.remark),
  };
}

function cloneReportItem(item = {}) {
  return {
    id: trimText(item.id) || nextDraftId("rep"),
    itemName: trimText(item.itemName),
    resultSummary: trimText(item.resultSummary),
    resultFlag: trimText(item.resultFlag) || "NORMAL",
    advice: trimText(item.advice),
  };
}

function normalizeDoctorOrders(items = []) {
  return items
    .map((item) => cloneDoctorOrderItem(item))
    .filter((item) => item.content);
}

function normalizePrescriptions(items = []) {
  return items
    .map((item) => clonePrescriptionItem(item))
    .filter((item) => item.drugName);
}

function normalizeReports(items = []) {
  return items
    .map((item) => cloneReportItem(item))
    .filter((item) => item.itemName || item.resultSummary);
}

export function fetchDoctorQueue() {
  return apiRequest("/api/doctor/clinic/queue");
}

export function startDoctorVisit(appointmentId) {
  return apiRequest(`/api/doctor/clinic/${appointmentId}/start`, {
    method: "POST",
  });
}

export function fetchDoctorRecords() {
  return apiRequest("/api/doctor/records");
}

export function fetchDoctorRecord(visitId) {
  return apiRequest(`/api/doctor/records/${visitId}`);
}

export function saveDoctorRecord(visitId, payload) {
  return apiRequest(`/api/doctor/records/${visitId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function completeDoctorRecord(visitId) {
  return apiRequest(`/api/doctor/records/${visitId}/complete`, {
    method: "POST",
  });
}

export function fetchDoctorPatients() {
  return apiRequest("/api/doctor/patients");
}

export function fetchPatientVisits() {
  return apiRequest("/api/patient/visits");
}

export function sortDoctorQueue(queue = []) {
  return [...queue].sort((left, right) => {
    const leftKey = `${left.date || ""} ${left.timeSlot || ""} ${left.paidAt || ""}`;
    const rightKey = `${right.date || ""} ${right.timeSlot || ""} ${right.paidAt || ""}`;
    return leftKey.localeCompare(rightKey);
  });
}

export function sortDoctorRecords(records = []) {
  return [...records].sort((left, right) => {
    if (left.status !== right.status) {
      return left.status === "IN_PROGRESS" ? -1 : 1;
    }
    return String(right.updatedAt || "").localeCompare(String(left.updatedAt || ""));
  });
}

export function pickCurrentVisit(records = []) {
  return sortDoctorRecords(records).find((item) => item.status === "IN_PROGRESS") || null;
}

export function buildDoctorOrdersDraft(record) {
  return {
    doctorOrders: normalizeDoctorOrders(record?.doctorOrders || []),
    prescriptions: normalizePrescriptions(record?.prescriptions || []),
    reports: normalizeReports(record?.reports || []),
  };
}

export function buildVisitRecordForm(record) {
  const draft = buildDoctorOrdersDraft(record);
  return {
    chiefComplaint: trimText(record?.chiefComplaint),
    diagnosis: trimText(record?.diagnosis),
    treatmentPlan: trimText(record?.treatmentPlan),
    doctorOrders: draft.doctorOrders,
    prescriptions: draft.prescriptions,
    reports: draft.reports,
  };
}

export function buildStructuredCollectionsPayload(form = {}) {
  return {
    doctorOrders: normalizeDoctorOrders(form.doctorOrders || []),
    prescriptions: normalizePrescriptions(form.prescriptions || []),
    reports: normalizeReports(form.reports || []),
  };
}

export function buildVisitRecordPayload(form = {}) {
  return {
    chiefComplaint: trimText(form.chiefComplaint),
    diagnosis: trimText(form.diagnosis),
    treatmentPlan: trimText(form.treatmentPlan),
    ...buildStructuredCollectionsPayload(form),
  };
}

export function sortDoctorPatients(patients = []) {
  return [...patients].sort((left, right) => String(right.latestVisitDate || "").localeCompare(String(left.latestVisitDate || "")));
}