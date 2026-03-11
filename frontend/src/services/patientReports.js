function isEligibleAppointment(appointment, today) {
  return appointment.status === "BOOKED" && appointment.paymentStatus === "PAID" && appointment.date <= today;
}

function hasRealReport(record) {
  return record.status === "COMPLETED" && String(record.reportNote || "").trim();
}

function buildReportNo(value) {
  const tail = String(value || "").slice(-4);
  return `REP-${tail.padStart(4, "0")}`;
}

function getReportItem(department) {
  const itemMap = {
    心内科: "心内科随访报告",
    呼吸内科: "呼吸内科随访报告",
    骨科: "骨科复诊报告",
    神经内科: "神经内科随访报告",
  };
  return itemMap[department] || "门诊随访报告";
}

function getFallbackResult(department) {
  if (department === "心内科" || department === "呼吸内科") {
    return "待复核";
  }
  return "正常";
}

function buildFallbackSummary(appointment) {
  return `${appointment.department}${appointment.doctorName}于${appointment.date}完成门诊随访记录整理，可查看本次报告摘要。`;
}

function buildFallbackAdvice(appointment, today) {
  if (appointment.date === today) {
    return "建议结合当日就诊情况留意后续随访通知。";
  }
  return "如症状持续或有疑问，建议按需复诊并携带本报告。";
}

function buildRealAdvice(record, today) {
  if (String(record.treatmentPlan || "").trim()) {
    return record.treatmentPlan;
  }
  if (record.visitDate === today) {
    return "请按医生建议留意用药和复诊安排。";
  }
  return "如有不适，请按医嘱及时复诊。";
}

function mapVisitReport(record, today) {
  return {
    id: record.id,
    serialNumber: record.appointmentId,
    reportNo: buildReportNo(record.id),
    item: getReportItem(record.department),
    date: record.completedAt || `${record.visitDate || ""} ${record.visitTimeSlot || ""}`.trim(),
    result: "已出具",
    summary: String(record.reportNote || "").trim(),
    advice: buildRealAdvice(record, today),
    department: record.department,
    doctorName: record.doctorName,
    source: "医生接诊",
  };
}

function mapAppointmentReport(appointment, today) {
  return {
    id: appointment.id,
    serialNumber: appointment.serialNumber,
    reportNo: buildReportNo(appointment.serialNumber),
    item: getReportItem(appointment.department),
    date: appointment.paidAt || `${appointment.date} ${appointment.timeSlot}`,
    result: getFallbackResult(appointment.department),
    summary: buildFallbackSummary(appointment),
    advice: buildFallbackAdvice(appointment, today),
    department: appointment.department,
    doctorName: appointment.doctorName,
    source: "预约映射",
  };
}

export function buildPatientReports({ appointments = [], visitRecords = [], today }) {
  const realReports = visitRecords
    .filter(hasRealReport)
    .sort(
      (left, right) =>
        String(right.completedAt || "").localeCompare(String(left.completedAt || "")) ||
        String(right.visitDate || "").localeCompare(String(left.visitDate || "")) ||
        String(right.visitTimeSlot || "").localeCompare(String(left.visitTimeSlot || ""))
    )
    .map((record) => mapVisitReport(record, today));

  if (realReports.length > 0) {
    return realReports;
  }

  return appointments
    .filter((appointment) => isEligibleAppointment(appointment, today))
    .sort(
      (left, right) =>
        String(right.date).localeCompare(String(left.date)) ||
        String(right.timeSlot || "").localeCompare(String(left.timeSlot || ""))
    )
    .map((appointment) => mapAppointmentReport(appointment, today));
}
