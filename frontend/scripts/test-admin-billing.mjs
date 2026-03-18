import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { buildAdminBillingModel } from "../src/services/adminBilling.js";

const overview = {
  cards: {
    unpaidCount: 1,
    paidCount: 1,
    refundedCount: 1,
  },
  bills: [
    {
      id: "BILL-UNPAID",
      serialNumber: "AP202603110900AAAA",
      patientId: "P1001",
      patientName: "张晓雪",
      department: "心内科",
      doctorName: "李敏医生",
      date: "2026-03-11",
      timeSlot: "09:00-09:30",
      status: "BOOKED",
      paymentStatus: "UNPAID",
      fee: 28,
      createdAt: "2026-03-11 09:00:00",
      paidAt: null,
    },
    {
      id: "BILL-PAID",
      serialNumber: "AP202603111030BBBB",
      patientId: "P1001",
      patientName: "张晓雪",
      department: "呼吸内科",
      doctorName: "赵晴",
      date: "2026-03-11",
      timeSlot: "10:00-10:30",
      status: "BOOKED",
      paymentStatus: "PAID",
      fee: 20,
      createdAt: "2026-03-11 10:30:00",
      paidAt: "2026-03-11 10:35:00",
    },
    {
      id: "BILL-REFUNDED",
      serialNumber: "AP202603111100CCCC",
      patientId: "P1001",
      patientName: "张晓雪",
      department: "骨科",
      doctorName: "陈宇",
      date: "2026-03-12",
      timeSlot: "09:30-10:00",
      status: "CANCELLED",
      paymentStatus: "REFUNDED",
      fee: 22,
      createdAt: "2026-03-11 11:00:00",
      paidAt: "2026-03-11 11:05:00",
    },
  ],
};

const model = buildAdminBillingModel(overview);
assert.equal(model.cards.length, 3);
assert.equal(model.cards[0].label, "待支付账单");
assert.equal(model.cards[0].value, "1");
assert.equal(model.cards[0].desc, "待收金额 28.00 元");
assert.equal(model.cards[1].label, "已支付账单");
assert.equal(model.cards[1].desc, "已收金额 20.00 元");
assert.equal(model.cards[2].label, "已退款账单");
assert.equal(model.cards[2].desc, "已退金额 22.00 元");
assert.equal(model.bills.length, 3);
assert.equal(model.bills[0].patientLabel, "张晓雪（P1001）");
assert.equal(model.bills[0].visitTime, "2026-03-11 09:00-09:30");
assert.equal(model.bills[0].amountLabel, "28.00 元");
assert.equal(model.bills[0].statusLabel, "已预约");
assert.equal(model.bills[0].paymentStatusLabel, "待支付");
assert.equal(model.bills[0].paidAtLabel, "未支付");
assert.equal(model.bills[2].statusLabel, "已取消");
assert.equal(model.bills[2].paymentStatusLabel, "已退款");
assert.equal(model.emptyHint, "当前暂无收费记录");

const billingView = await readFile(new URL("../src/views/admin/AdminBilling.vue", import.meta.url), "utf8");
assert.match(billingView, /buildAdminBillingModel/);
assert.match(billingView, /fetchAdminBillingOverview/);
assert.match(billingView, /billingModel\.cards/);
assert.match(billingView, /billingModel\.bills/);
assert.doesNotMatch(billingView, /:lg="17"/);
assert.doesNotMatch(billingView, /:lg="7"/);
assert.doesNotMatch(billingView, /note-card/);
assert.doesNotMatch(billingView, /const chargeItems =/);
assert.doesNotMatch(billingView, /const exceptions =/);

console.log("admin billing mapping tests passed");