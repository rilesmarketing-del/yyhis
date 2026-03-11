function isPaidBookedAppointment(appointment) {
  return appointment.status === "BOOKED" && appointment.paymentStatus === "PAID";
}

function buildFallbackVisitNo(serialNumber) {
  const tail = String(serialNumber || "").slice(-4);
  return `VIS-${tail.padStart(4, "0")}`;
}

function buildRecordVisitNo(visitId) {
  const tail = String(visitId || "").slice(-4);
  return `VIS-${tail.padStart(4, "0")}`;
}

function buildNextStep(date, today) {
  if (date > today) {
    return "请按预约时间到院就诊";
  }
  return "如已完成就诊，可在后续版本查看完整病历";
}

function buildAppointmentMap(appointments) {
  return new Map((appointments || []).map((item) => [item.id, item]));
}

function resolveSerialNumber(appointmentMap, appointmentId) {
  return appointmentMap.get(appointmentId)?.serialNumber || appointmentId;
}

function resolvePaidAt(appointmentMap, appointmentId) {
  return appointmentMap.get(appointmentId)?.paidAt || "-";
}

function mapVisitRecord(record, appointmentMap) {
  const detail = [record.diagnosis, record.treatmentPlan, record.doctorOrderNote].filter(Boolean).join(" / ");
  return {
    id: record.id,
    appointmentId: record.appointmentId,
    serialNumber: resolveSerialNumber(appointmentMap, record.appointmentId),
    visitNo: buildRecordVisitNo(record.id),
    dept: record.department,
    doctor: record.doctorName,
    date: `${record.visitDate} ${record.visitTimeSlot}`,
    appointmentDate: record.visitDate,
    appointmentTimeSlot: record.visitTimeSlot,
    paymentStatus: "PAID",
    paidAt: resolvePaidAt(appointmentMap, record.appointmentId),
    source: "医生接诊",
    status: record.status === "COMPLETED" ? "已完成" : "接诊中",
    nextStep: record.status === "COMPLETED" ? "可查看医生诊断与处理建议" : "医生正在接诊，完成后可查看详细记录",
    diagnosis: record.diagnosis,
    treatmentPlan: record.treatmentPlan,
    doctorOrderNote: record.doctorOrderNote,
    prescriptionNote: record.prescriptionNote,
    reportNote: record.reportNote,
    detail,
  };
}

function mapAppointmentVisit(appointment, today) {
  return {
    id: appointment.id,
    appointmentId: appointment.id,
    serialNumber: appointment.serialNumber,
    visitNo: buildFallbackVisitNo(appointment.serialNumber),
    dept: appointment.department,
    doctor: appointment.doctorName,
    date: `${appointment.date} ${appointment.timeSlot}`,
    appointmentDate: appointment.date,
    appointmentTimeSlot: appointment.timeSlot,
    paymentStatus: appointment.paymentStatus,
    paidAt: appointment.paidAt || "-",
    source: "预约挂号",
    status: "已登记",
    nextStep: buildNextStep(appointment.date, today),
  };
}

export function buildPatientVisits({ appointments = [], visitRecords = [], today }) {
  const appointmentMap = buildAppointmentMap(appointments);

  if (visitRecords.length > 0) {
    return [...visitRecords]
      .sort((left, right) => String(right.visitDate || "").localeCompare(String(left.visitDate || "")) || String(right.visitTimeSlot || "").localeCompare(String(left.visitTimeSlot || "")))
      .map((record) => mapVisitRecord(record, appointmentMap));
  }

  return appointments
    .filter(isPaidBookedAppointment)
    .sort((left, right) => String(right.date).localeCompare(String(left.date)) || String(right.timeSlot).localeCompare(String(left.timeSlot)))
    .map((appointment) => mapAppointmentVisit(appointment, today));
}