import assert from "node:assert/strict";
import { buildPatientDashboardModel } from "../src/services/patientDashboard.js";

const appointments = [
  {
    id: "a1",
    department: "心内科",
    doctorName: "李医生",
    date: "2026-03-10",
    timeSlot: "09:00-09:30",
    status: "BOOKED",
    paymentStatus: "UNPAID",
    createdAt: "2026-03-10 08:00:00",
  },
  {
    id: "a2",
    department: "骨科",
    doctorName: "王医生",
    date: "2026-03-10",
    timeSlot: "14:00-14:30",
    status: "BOOKED",
    paymentStatus: "PAID",
    createdAt: "2026-03-10 07:00:00",
  },
  {
    id: "a3",
    department: "呼吸内科",
    doctorName: "张医生",
    date: "2026-03-09",
    timeSlot: "10:00-10:30",
    status: "CANCELLED",
    paymentStatus: "REFUNDED",
    createdAt: "2026-03-09 11:00:00",
  },
  {
    id: "a4",
    department: "神经内科",
    doctorName: "陈医生",
    date: "2026-03-08",
    timeSlot: "15:00-15:30",
    status: "RESCHEDULED",
    paymentStatus: "UNPAID",
    createdAt: "2026-03-08 12:00:00",
  },
];

const model = buildPatientDashboardModel({
  appointments,
  today: "2026-03-10",
  currentMonth: "2026-03",
});

assert.equal(model.stats[0].value, "2");
assert.equal(model.stats[1].value, "1");
assert.equal(model.stats[2].value, "4");
assert.equal(model.stats[3].value, "1");
assert.equal(model.timeline[0].title, "已预约，待支付");
assert.equal(model.timeline[1].title, "预约已支付");
assert.equal(model.timeline[2].title, "预约已取消，已退款");
assert.equal(model.quickActions[2].desc, "当前有 1 笔待支付");

const emptyModel = buildPatientDashboardModel({
  appointments: [],
  today: "2026-03-10",
  currentMonth: "2026-03",
});

assert.equal(emptyModel.timeline.length, 1);
assert.equal(emptyModel.timeline[0].title, "还没有预约记录");
assert.equal(emptyModel.quickActions[2].desc, "暂无待支付账单");

console.log("patient dashboard mapping tests passed");