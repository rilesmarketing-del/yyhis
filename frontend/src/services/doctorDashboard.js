function getRecordStatusLabel(status) {
  if (status === "IN_PROGRESS") {
    return "接诊中";
  }
  if (status === "COMPLETED") {
    return "已完成";
  }
  return status || "-";
}

function buildStats({ queue, todayRecords, completedToday, inProgressRecords, patients }) {
  return [
    {
      label: "今日接诊",
      value: String(todayRecords.length),
      desc: `接诊中 ${inProgressRecords.length} / 已完成 ${completedToday.length}`,
    },
    {
      label: "当前候诊",
      value: String(queue.length),
      desc: queue.length > 0 ? "待接诊患者队列" : "当前暂无候诊患者",
    },
    {
      label: "待完成病历",
      value: String(inProgressRecords.length),
      desc: inProgressRecords.length > 0 ? "请及时补全并结束接诊" : "当前没有待完成病历",
    },
    {
      label: "累计接诊患者",
      value: String(patients.length),
      desc: "已进入医生患者列表的人数",
    },
  ];
}

function buildOverview(queue, todayRecords) {
  const queueItems = queue.map((item) => ({
    id: `queue-${item.appointmentId}`,
    patientName: item.patientName,
    department: item.department,
    time: `${item.date} ${item.timeSlot}`,
    status: "待接诊",
    source: "候诊队列",
    sortKey: `${item.date || ""} ${item.timeSlot || ""}`,
    order: 0,
  }));

  const recordItems = todayRecords.map((item) => ({
    id: `record-${item.id}`,
    patientName: item.patientName,
    department: item.department,
    time: `${item.visitDate} ${item.visitTimeSlot}`,
    status: getRecordStatusLabel(item.status),
    source: "接诊记录",
    sortKey: `${item.visitDate || ""} ${item.visitTimeSlot || ""}`,
    order: item.status === "IN_PROGRESS" ? 1 : 2,
  }));

  return [...queueItems, ...recordItems].sort(
    (left, right) => left.order - right.order || String(left.sortKey).localeCompare(String(right.sortKey))
  );
}

function buildTodos({ queue, completedToday, inProgressRecords, patients }) {
  const todos = [];

  if (queue.length > 0) {
    todos.push({ id: "queue", type: "warning", text: `当前有 ${queue.length} 位患者待接诊` });
  }
  if (inProgressRecords.length > 0) {
    todos.push({ id: "records", type: "primary", text: `有 ${inProgressRecords.length} 份病历待完成` });
  }
  if (completedToday.length > 0) {
    todos.push({ id: "completed", type: "success", text: `今日已完成 ${completedToday.length} 次接诊` });
  }
  if (patients.length > 0 && todos.length < 3) {
    todos.push({ id: "patients", type: "info", text: `当前累计维护 ${patients.length} 位患者档案` });
  }
  if (todos.length === 0) {
    todos.push({ id: "clear", type: "success", text: "当前工作已清空，可查看历史患者" });
  }

  return todos.slice(0, 5);
}

export function buildDoctorDashboardModel({ queue = [], records = [], patients = [], today }) {
  const todayRecords = records.filter((item) => item.visitDate === today && ["IN_PROGRESS", "COMPLETED"].includes(item.status));
  const completedToday = todayRecords.filter((item) => item.status === "COMPLETED");
  const inProgressRecords = records.filter((item) => item.status === "IN_PROGRESS");

  return {
    stats: buildStats({ queue, todayRecords, completedToday, inProgressRecords, patients }),
    overview: buildOverview(queue, todayRecords),
    todos: buildTodos({ queue, completedToday, inProgressRecords, patients }),
    quickActions: [
      { label: "去接诊台", path: "/doctor/clinic", type: "primary" },
      { label: "去病历页", path: "/doctor/records", type: "success" },
      { label: "去工作台", path: "/doctor/orders", type: "warning" },
      { label: "去患者列表", path: "/doctor/patients", type: "info" },
    ],
  };
}
