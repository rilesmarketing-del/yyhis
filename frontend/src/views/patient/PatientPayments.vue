<template>
  <div class="payments-page">
    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">缴费中心</div>
            <div class="panel-subtitle">查看当前患者的挂号待支付账单和支付记录</div>
          </div>
          <div class="header-actions">
            <el-tag type="danger">待支付 {{ unpaidCount }} 笔</el-tag>
            <el-button @click="loadPayments">刷新数据</el-button>
          </div>
        </div>
      </template>

      <el-alert
        :title="`当前登录患者：${activePatient.name}`"
        type="success"
        :closable="false"
        show-icon
        class="patient-alert"
      />

      <el-tabs v-model="activeTab">
        <el-tab-pane :label="`待支付账单（${unpaidCount}）`" name="unpaid">
          <div class="section-meta">
            <span>仅展示预约挂号产生的待支付账单</span>
            <span v-if="loading">账单加载中...</span>
          </div>

          <el-table :data="unpaidBills" border v-loading="loading" empty-text="暂无待支付账单">
            <el-table-column prop="serialNumber" label="账单号" min-width="180" />
            <el-table-column prop="typeLabel" label="费用类型" width="110" />
            <el-table-column label="就诊信息" min-width="220">
              <template #default="{ row }">
                {{ row.department }} / {{ row.doctorName }} / {{ row.date }} {{ row.timeSlot }}
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">{{ formatFee(row.fee) }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="生成时间" min-width="170" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link :loading="paySubmittingId === row.id" @click="handlePay(row)">
                  立即支付
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && unpaidBills.length === 0" description="暂无待支付账单" />
        </el-tab-pane>

        <el-tab-pane :label="`支付记录（${historyBills.length}）`" name="history">
          <div class="section-meta">
            <span>展示已支付和已退款的挂号支付记录</span>
            <span v-if="loading">支付记录加载中...</span>
          </div>

          <el-table :data="historyBills" border v-loading="loading">
            <el-table-column prop="serialNumber" label="账单号" min-width="180" />
            <el-table-column prop="typeLabel" label="费用类型" width="110" />
            <el-table-column label="就诊信息" min-width="220">
              <template #default="{ row }">
                {{ row.department }} / {{ row.doctorName }} / {{ row.date }} {{ row.timeSlot }}
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">{{ formatFee(row.fee) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="paymentStatusMeta[row.paymentStatus]?.type || 'info'">
                  {{ paymentStatusMeta[row.paymentStatus]?.label || row.paymentStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="支付时间" min-width="170">
              <template #default="{ row }">{{ row.paidAt || '-' }}</template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && historyBills.length === 0" description="暂无支付记录" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { fetchMyAppointments, payAppointment } from "../../services/patientAppointments";
import { getActivePatient } from "../../services/patientSession";

const activeTab = ref("unpaid");
const activePatient = ref(getActivePatient());
const appointments = ref([]);
const loading = ref(false);
const paySubmittingId = ref("");

const paymentStatusMeta = {
  PAID: { label: "已支付", type: "success" },
  REFUNDED: { label: "已退款", type: "info" },
};

const paymentRows = computed(() => {
  return appointments.value.map((appointment) => ({
    ...appointment,
    typeLabel: "挂号费",
  }));
});

const unpaidBills = computed(() => {
  return paymentRows.value.filter((item) => item.status === "BOOKED" && item.paymentStatus === "UNPAID");
});

const historyBills = computed(() => {
  return paymentRows.value.filter((item) => item.paymentStatus === "PAID" || item.paymentStatus === "REFUNDED");
});

const unpaidCount = computed(() => unpaidBills.value.length);

function formatFee(fee) {
  return `${Number(fee || 0).toFixed(2)} 元`;
}

async function loadPayments() {
  loading.value = true;
  activePatient.value = getActivePatient();
  try {
    appointments.value = await fetchMyAppointments();
  } catch (error) {
    ElMessage.error(error.message || "支付信息加载失败");
  } finally {
    loading.value = false;
  }
}

async function handlePay(row) {
  paySubmittingId.value = row.id;
  try {
    await ElMessageBox.confirm(`确认支付账单 ${row.serialNumber}，金额 ${formatFee(row.fee)} 吗？`, "虚拟支付", {
      type: "warning",
    });
    await payAppointment(row.id);
    ElMessage.success("支付成功");
    await loadPayments();
    activeTab.value = "history";
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "支付失败");
    }
  } finally {
    paySubmittingId.value = "";
  }
}

onMounted(() => {
  loadPayments();
});
</script>

<style scoped>
.payments-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-card {
  margin-bottom: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #164e63;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.patient-alert {
  margin-bottom: 16px;
}

.section-meta {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}

@media (max-width: 900px) {
  .header-row,
  .section-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
