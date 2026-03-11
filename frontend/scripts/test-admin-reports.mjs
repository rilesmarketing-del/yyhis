import assert from "node:assert/strict";
import { buildAdminOperationsReportModel } from "../src/services/adminReports.js";

const report = {
  cards: {
    activeSchedules: 5,
    todayAppointments: 2,
    todayVisits: 1,
    totalPatients: 1,
  },
  table: [
    { metric: "排班总数", value: "5" },
    { metric: "今日启用排班", value: "3" },
    { metric: "今日预约数", value: "2" },
    { metric: "今日接诊数", value: "1" },
    { metric: "今日完成接诊数", value: "1" },
    { metric: "当前待接诊数", value: "1" },
    { metric: "当前进行中病历数", value: "0" },
    { metric: "累计患者数", value: "1" },
  ],
};

const model = buildAdminOperationsReportModel(report);
assert.equal(model.cards[0].label, "启用中排班");
assert.equal(model.cards[0].value, "5");
assert.equal(model.cards[3].label, "累计患者");
assert.equal(model.cards[3].value, "1");
assert.equal(model.table.length, 8);
assert.equal(model.table[0].metric, "排班总数");
assert.equal(model.table[5].value, "1");

console.log("admin operations report mapping tests passed");