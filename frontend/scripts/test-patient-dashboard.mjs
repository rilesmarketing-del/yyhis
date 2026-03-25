import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { buildPatientDashboardModel, buildPatientRegistrationGuide } from "../src/services/patientDashboard.js";

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
assert.equal(model.quickActions[3].desc, "查看真实报告与复诊建议");

const emptyModel = buildPatientDashboardModel({
  appointments: [],
  today: "2026-03-10",
  currentMonth: "2026-03",
});

assert.equal(emptyModel.timeline.length, 1);
assert.equal(emptyModel.timeline[0].title, "还没有预约记录");
assert.equal(emptyModel.quickActions[2].desc, "暂无待支付账单");

const guide = buildPatientRegistrationGuide({
  displayName: "新患者",
  patientId: "P1088",
});

assert.equal(guide.title, "欢迎来到患者个人中心");
assert.equal(guide.patientLabel, "患者服务已准备就绪");
assert.equal(guide.primaryAction.label, "去预约挂号");
assert.equal(guide.secondaryAction.label, "稍后再说");
assert.match(guide.description, /新患者/);

assert.equal(buildPatientRegistrationGuide(null), null);

const patientDashboardView = await readFile(new URL("../src/views/patient/PatientDashboard.vue", import.meta.url), "utf8");
assert.doesNotMatch(patientDashboardView, /activePatient\.id/);
assert.doesNotMatch(patientDashboardView, /当前登录患者：\$\{activePatient\.name\}（\$\{activePatient\.id\}）/);
assert.doesNotMatch(patientDashboardView, /围绕当前患者真实预约与就诊记录生成的温和型总览/);
assert.doesNotMatch(patientDashboardView, /今日关注/);
assert.doesNotMatch(patientDashboardView, /安心就诊链路/);
assert.match(patientDashboardView, /\.hero-shell\s*\{[\s\S]*align-items:\s*center;/);
assert.match(patientDashboardView, /\.hero-eyebrow\s*\{[\s\S]*font-size:\s*15px;/);
assert.match(patientDashboardView, /\.panel-title\s*\{[\s\S]*font-size:\s*42px;/);
assert.match(patientDashboardView, /\.panel-title\s*\{[\s\S]*line-height:\s*1\.05;/);

console.log("patient dashboard mapping tests passed");
