function hasStructuredPrescriptions(record) {
  return record.status === "COMPLETED" && Array.isArray(record.prescriptions) && record.prescriptions.length > 0;
}

function hasPrescription(record) {
  return record.status === "COMPLETED" && (hasStructuredPrescriptions(record) || String(record.prescriptionNote || "").trim());
}

function buildPrescriptionNo(visitId) {
  const tail = String(visitId || "").slice(-4);
  return `RX-${tail.padStart(4, "0")}`;
}

function normalizePrescriptionItem(item = {}) {
  return {
    id: String(item.id || "").trim(),
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

function buildStructuredContent(items = []) {
  return items.map((item) => describePrescriptionItem(item)).filter(Boolean).join("；");
}

export function buildPatientPrescriptions({ visitRecords = [] }) {
  return visitRecords
    .filter(hasPrescription)
    .sort(
      (left, right) =>
        String(right.completedAt || "").localeCompare(String(left.completedAt || "")) ||
        String(right.visitDate || "").localeCompare(String(left.visitDate || ""))
    )
    .map((record) => {
      const items = hasStructuredPrescriptions(record)
        ? record.prescriptions.map((item) => normalizePrescriptionItem(item)).filter((item) => item.drugName)
        : [];
      const content = items.length > 0 ? buildStructuredContent(items) : String(record.prescriptionNote || "").trim();

      return {
        id: record.id,
        presNo: buildPrescriptionNo(record.id),
        dept: record.department,
        doctorName: record.doctorName,
        date: record.completedAt || record.visitDate || "-",
        status: "已开立",
        content,
        items,
        appointmentId: record.appointmentId,
      };
    });
}