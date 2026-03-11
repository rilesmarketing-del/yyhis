function hasPrescription(record) {
  return record.status === "COMPLETED" && String(record.prescriptionNote || "").trim();
}

function buildPrescriptionNo(visitId) {
  const tail = String(visitId || "").slice(-4);
  return `RX-${tail.padStart(4, "0")}`;
}

export function buildPatientPrescriptions({ visitRecords = [] }) {
  return visitRecords
    .filter(hasPrescription)
    .sort(
      (left, right) =>
        String(right.completedAt || "").localeCompare(String(left.completedAt || "")) ||
        String(right.visitDate || "").localeCompare(String(left.visitDate || ""))
    )
    .map((record) => ({
      id: record.id,
      presNo: buildPrescriptionNo(record.id),
      dept: record.department,
      doctorName: record.doctorName,
      date: record.completedAt || record.visitDate || "-",
      status: "已开立",
      content: String(record.prescriptionNote || "").trim(),
      appointmentId: record.appointmentId,
    }));
}
