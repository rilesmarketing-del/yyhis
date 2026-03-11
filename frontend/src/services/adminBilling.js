import { apiRequest } from "./apiClient.js";

const statusMeta = {
  BOOKED: { label: "已预约", type: "primary" },
  CANCELLED: { label: "已取消", type: "info" },
  RESCHEDULED: { label: "已改约", type: "warning" },
};

const paymentStatusMeta = {
  UNPAID: { label: "待支付", type: "warning" },
  PAID: { label: "已支付", type: "success" },
  REFUNDED: { label: "已退款", type: "info" },
};

export function fetchAdminBillingOverview() {
  return apiRequest("/api/admin/billing/overview");
}

export function buildAdminBillingModel(overview = {}) {
  const cards = overview.cards || {};
  const bills = Array.isArray(overview.bills) ? overview.bills : [];

  return {
    cards: [
      {
        label: "待支付账单",
        value: String(cards.unpaidCount ?? 0),
        desc: `待收金额 ${formatAmount(sumFeeByStatus(bills, "UNPAID"))}`,
      },
      {
        label: "已支付账单",
        value: String(cards.paidCount ?? 0),
        desc: `已收金额 ${formatAmount(sumFeeByStatus(bills, "PAID"))}`,
      },
      {
        label: "已退款账单",
        value: String(cards.refundedCount ?? 0),
        desc: `已退金额 ${formatAmount(sumFeeByStatus(bills, "REFUNDED"))}`,
      },
    ],
    bills: bills.map((item) => ({
      id: item.id,
      serialNumber: item.serialNumber || "",
      patientLabel: buildPatientLabel(item.patientName, item.patientId),
      department: item.department || "",
      doctorName: item.doctorName || "",
      visitTime: [item.date, item.timeSlot].filter(Boolean).join(" "),
      amountLabel: formatAmount(item.fee),
      statusLabel: statusMeta[item.status]?.label || item.status || "",
      statusType: statusMeta[item.status]?.type || "info",
      paymentStatusLabel: paymentStatusMeta[item.paymentStatus]?.label || item.paymentStatus || "",
      paymentStatusType: paymentStatusMeta[item.paymentStatus]?.type || "info",
      paidAtLabel: item.paidAt || "未支付",
      createdAt: item.createdAt || "",
    })),
    emptyHint: "当前暂无收费记录",
  };
}

function buildPatientLabel(patientName, patientId) {
  if (patientName && patientId) {
    return `${patientName}（${patientId}）`;
  }
  return patientName || patientId || "未知患者";
}

function sumFeeByStatus(bills, paymentStatus) {
  return bills
    .filter((item) => item.paymentStatus === paymentStatus)
    .reduce((total, item) => total + toNumber(item.fee), 0);
}

function formatAmount(value) {
  return `${toNumber(value).toFixed(2)} 元`;
}

function toNumber(value) {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue : 0;
}