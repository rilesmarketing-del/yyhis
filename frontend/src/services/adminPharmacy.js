import { apiRequest } from "./apiClient.js";

const visitStatusMeta = {
  IN_PROGRESS: { label: "接诊中", type: "warning" },
  COMPLETED: { label: "已完成", type: "success" },
};

export function fetchAdminPharmacyOverview() {
  return apiRequest("/api/admin/pharmacy/overview");
}

function formatPatientLabel(name, patientId) {
  if (name && patientId) {
    return `${name}（${patientId}）`;
  }
  return name || patientId || "-";
}

function formatVisitTime(date, timeSlot) {
  return [date, timeSlot].filter(Boolean).join(" ") || "-";
}

function normalizeText(value, fallback = "-") {
  const text = String(value || "").trim();
  return text || fallback;
}

function normalizePrescriptionItem(item = {}) {
  return {
    drugName: String(item.drugName || "").trim(),
    dosage: String(item.dosage || "").trim(),
    frequency: String(item.frequency || "").trim(),
    days: String(item.days || "").trim(),
    remark: String(item.remark || "").trim(),
  };
}

function describePrescriptionItem(item) {
  return [item.drugName, item.dosage, item.frequency, item.days ? `${item.days}天` : "", item.remark]
    .filter(Boolean)
    .join("，");
}

function buildPrescriptionPreview(item) {
  const prescriptions = Array.isArray(item.prescriptions)
    ? item.prescriptions.map((entry) => normalizePrescriptionItem(entry)).filter((entry) => entry.drugName)
    : [];
  if (prescriptions.length > 0) {
    return {
      preview: prescriptions.map((entry) => describePrescriptionItem(entry)).join("；"),
      count: prescriptions.length,
    };
  }
  return {
    preview: normalizeText(item.prescriptionNote),
    count: 0,
  };
}

export function buildAdminPharmacyModel(overview = {}) {
  const cards = overview.cards || {};
  const records = Array.isArray(overview.records) ? overview.records : [];

  return {
    cards: [
      { label: "处方记录数", value: String(cards.totalPrescriptions ?? 0), desc: "已填写处方内容的接诊记录" },
      { label: "今日处方", value: String(cards.todayPrescriptions ?? 0), desc: "按最近保存时间统计今日处方" },
      { label: "涉及患者", value: String(cards.patientCount ?? 0), desc: "去重患者数量" },
    ],
    records: records.map((item) => {
      const statusMeta = visitStatusMeta[item.status] || { label: item.status || "未知", type: "info" };
      const prescriptionMeta = buildPrescriptionPreview(item);
      return {
        id: item.id,
        patientLabel: formatPatientLabel(item.patientName, item.patientId),
        department: item.department || "-",
        doctorName: item.doctorName || "-",
        visitTime: formatVisitTime(item.visitDate, item.visitTimeSlot),
        statusLabel: statusMeta.label,
        statusType: statusMeta.type,
        prescriptionPreview: prescriptionMeta.preview,
        structuredCount: prescriptionMeta.count,
      };
    }),
    emptyHint: "当前暂无处方记录",
    note: "当前页面优先展示结构化处方条目，同时保留对文字处方内容的兼容，不代表药品库存实物数量，库存与出入库能力将在后续补齐。",
  };
}
