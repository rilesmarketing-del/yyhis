function countTodayBooked(appointments, today) {
  return appointments.filter((item) => item.date === today && item.status === "BOOKED").length;
}

function countPendingPayments(appointments) {
  return appointments.filter((item) => item.status === "BOOKED" && item.paymentStatus === "UNPAID").length;
}

function countCurrentMonthAppointments(appointments, currentMonth) {
  return appointments.filter((item) => String(item.createdAt || "").startsWith(currentMonth)).length;
}

function countPaidAppointments(appointments) {
  return appointments.filter((item) => item.paymentStatus === "PAID").length;
}

function describeTodayBooked(count) {
  return count > 0 ? `今日待就诊 ${count} 次` : "暂无今日预约";
}

function describePendingPayments(count) {
  return count > 0 ? `当前有 ${count} 笔待支付` : "暂无待支付账单";
}

function describeMonthAppointments(count) {
  return count > 0 ? `本月累计创建 ${count} 条预约` : "本月还没有预约记录";
}

function describePaidAppointments(count) {
  return count > 0 ? `已有 ${count} 条预约完成支付` : "暂无已支付预约";
}

function getTimelineTitle(appointment) {
  if (appointment.status === "BOOKED" && appointment.paymentStatus === "UNPAID") {
    return "已预约，待支付";
  }
  if (appointment.status === "BOOKED" && appointment.paymentStatus === "PAID") {
    return "预约已支付";
  }
  if (appointment.status === "CANCELLED" && appointment.paymentStatus === "REFUNDED") {
    return "预约已取消，已退款";
  }
  if (appointment.status === "CANCELLED") {
    return "预约已取消";
  }
  if (appointment.status === "RESCHEDULED") {
    return "原预约已改约";
  }
  return "预约状态已更新";
}

function getTimelineType(appointment) {
  if (appointment.status === "BOOKED" && appointment.paymentStatus === "PAID") {
    return "success";
  }
  if (appointment.status === "BOOKED") {
    return "primary";
  }
  if (appointment.status === "CANCELLED") {
    return "warning";
  }
  if (appointment.status === "RESCHEDULED") {
    return "info";
  }
  return "primary";
}

function buildTimelineItem(appointment) {
  return {
    id: appointment.id,
    time: appointment.createdAt || `${appointment.date} ${appointment.timeSlot}`,
    title: getTimelineTitle(appointment),
    desc: `${appointment.department} / ${appointment.doctorName} / ${appointment.date} ${appointment.timeSlot}`,
    type: getTimelineType(appointment),
  };
}

function buildEmptyTimeline() {
  return [
    {
      id: "empty",
      time: "现在",
      title: "还没有预约记录",
      desc: "去预约挂号创建第一条记录后，这里会显示你的最近动态。",
      type: "primary",
    },
  ];
}

export function buildPatientDashboardModel({ appointments, today, currentMonth }) {
  const todayBooked = countTodayBooked(appointments, today);
  const pendingPayments = countPendingPayments(appointments);
  const monthAppointments = countCurrentMonthAppointments(appointments, currentMonth);
  const paidAppointments = countPaidAppointments(appointments);

  const timeline = appointments.length
    ? [...appointments]
        .sort((left, right) => String(right.createdAt || "").localeCompare(String(left.createdAt || "")))
        .slice(0, 5)
        .map(buildTimelineItem)
    : buildEmptyTimeline();

  return {
    stats: [
      { label: "今日预约", value: String(todayBooked), desc: describeTodayBooked(todayBooked) },
      { label: "待支付账单", value: String(pendingPayments), desc: describePendingPayments(pendingPayments) },
      { label: "本月预约数", value: String(monthAppointments), desc: describeMonthAppointments(monthAppointments) },
      { label: "已支付预约", value: String(paidAppointments), desc: describePaidAppointments(paidAppointments) },
    ],
    timeline,
    quickActions: [
      { key: "create", label: "新建预约挂号", desc: todayBooked > 0 ? `今日待就诊 ${todayBooked} 次` : "随时创建新的预约", path: "/patient/appointments", type: "primary" },
      { key: "appointments", label: "查看我的预约", desc: appointments.length > 0 ? `当前共有 ${appointments.length} 条预约记录` : "还没有预约记录", path: "/patient/appointments", type: "success" },
      { key: "payments", label: "处理待支付账单", desc: describePendingPayments(pendingPayments), path: "/patient/payments", type: "warning" },
      { key: "reports", label: "查看检查报告", desc: "查看真实报告与复诊建议", path: "/patient/reports", type: "info" },
    ],
  };
}