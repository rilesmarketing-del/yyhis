<template>
  <div class="appointment-page">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">预约挂号</div>
                <div class="panel-subtitle">查询医生排班并完成挂号预约</div>
              </div>
              <el-button type="primary" @click="refreshAll">刷新数据</el-button>
            </div>
          </template>

          <el-alert
            :title="`当前登录患者：${activePatient.name}`"
            type="success"
            :closable="false"
            show-icon
            class="patient-alert"
          />

          <el-form :inline="true" :model="scheduleQuery" class="filter-form">
            <el-form-item label="科室">
              <el-select v-model="scheduleQuery.department" placeholder="全部科室" clearable style="width: 180px">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker
                v-model="scheduleQuery.date"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadSchedules">查询预约</el-button>
              <el-button @click="resetScheduleQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="section-meta">
            <span>共找到 {{ schedules.length }} 个可用号源</span>
            <span v-if="scheduleLoading">排班加载中...</span>
          </div>

          <el-table :data="schedules" border empty-text="暂无预约数据">
            <el-table-column prop="department" label="科室" width="120" />
            <el-table-column prop="doctorName" label="医生" width="110" />
            <el-table-column prop="title" label="职称" width="120" />
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="timeSlot" label="时段" width="130" />
            <el-table-column label="挂号费" width="100">
              <template #default="{ row }">{{ formatFee(row.fee) }}</template>
            </el-table-column>
            <el-table-column label="余号" width="90">
              <template #default="{ row }">
                <el-tag :type="scheduleTagType(row.remainingSlots)">
                  {{ row.remainingSlots }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link :disabled="row.remainingSlots <= 0" @click="openBookingDialog(row)">
                  预约该时段
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!scheduleLoading && schedules.length === 0" description="当前筛选条件下暂无可预约号源" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">我的预约</div>
                <div class="panel-subtitle">查看和管理当前患者的预约记录</div>
              </div>
            </div>
          </template>

          <el-form :inline="true" :model="appointmentFilter" class="filter-form compact-filter">
            <el-form-item label="业务状态">
              <el-select v-model="appointmentFilter.status" clearable placeholder="全部状态" style="width: 160px">
                <el-option label="已预约" value="BOOKED" />
                <el-option label="已取消" value="CANCELLED" />
                <el-option label="已改约" value="RESCHEDULED" />
              </el-select>
            </el-form-item>
            <el-form-item label="支付状态">
              <el-select v-model="appointmentFilter.paymentStatus" clearable placeholder="全部支付状态" style="width: 170px">
                <el-option label="待支付" value="UNPAID" />
                <el-option label="已支付" value="PAID" />
                <el-option label="已退款" value="REFUNDED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetAppointmentFilter">清空</el-button>
            </el-form-item>
          </el-form>

          <div class="section-meta">
            <span>当前记录 {{ filteredAppointments.length }} 条</span>
            <span v-if="appointmentLoading">预约记录加载中...</span>
          </div>

          <el-table :data="filteredAppointments" border>
            <el-table-column prop="serialNumber" label="预约单号" min-width="170" />
            <el-table-column prop="department" label="科室" width="100" />
            <el-table-column prop="doctorName" label="医生" width="100" />
            <el-table-column label="时间" min-width="150">
              <template #default="{ row }">{{ row.date }} {{ row.timeSlot }}</template>
            </el-table-column>
            <el-table-column label="业务状态" width="100">
              <template #default="{ row }">
                <el-tag :type="appointmentStatusMeta[row.status]?.type || 'info'">
                  {{ appointmentStatusMeta[row.status]?.label || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="支付状态" width="100">
              <template #default="{ row }">
                <el-tag :type="paymentStatusMeta[row.paymentStatus]?.type || 'info'">
                  {{ paymentStatusMeta[row.paymentStatus]?.label || row.paymentStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" min-width="160" />
            <el-table-column label="操作" min-width="190" fixed="right">
              <template #default="{ row }">
                <div class="action-group" v-if="row.status === 'BOOKED' && row.paymentStatus === 'UNPAID'">
                  <el-button type="success" link :loading="paySubmittingId === row.id" @click="handlePay(row)">
                    虚拟支付
                  </el-button>
                  <el-button type="primary" link @click="openRescheduleDialog(row)">改约</el-button>
                  <el-button type="danger" link @click="handleCancel(row)">取消预约</el-button>
                </div>
                <el-button
                  v-else-if="row.status === 'BOOKED' && row.paymentStatus === 'PAID'"
                  type="danger"
                  link
                  @click="handleCancel(row)"
                >
                  取消预约
                </el-button>
                <span v-else class="muted-text">不可操作</span>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!appointmentLoading && filteredAppointments.length === 0" description="暂无预约记录" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="bookingDialogVisible" title="确认预约" width="520px">
      <el-descriptions v-if="selectedSchedule" :column="1" border>
        <el-descriptions-item label="患者">{{ activePatient.name }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ selectedSchedule.department }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ selectedSchedule.doctorName }} / {{ selectedSchedule.title }}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ selectedSchedule.date }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ selectedSchedule.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="挂号费">{{ formatFee(selectedSchedule.fee) }}</el-descriptions-item>
        <el-descriptions-item label="剩余号源">{{ selectedSchedule.remainingSlots }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookingSubmitting" @click="confirmBooking">确认预约</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rescheduleDialogVisible" title="改约" width="760px">
      <div v-if="rescheduleSourceAppointment" class="reschedule-dialog">
        <el-alert
          title="仅未支付预约支持改约，改约后会生成一条新的待支付预约记录。"
          type="warning"
          :closable="false"
          show-icon
          class="reschedule-alert"
        />

        <el-descriptions :column="1" border class="reschedule-summary">
          <el-descriptions-item label="原预约单号">{{ rescheduleSourceAppointment.serialNumber }}</el-descriptions-item>
          <el-descriptions-item label="原预约时段">
            {{ rescheduleSourceAppointment.department }} / {{ rescheduleSourceAppointment.doctorName }} /
            {{ rescheduleSourceAppointment.date }} {{ rescheduleSourceAppointment.timeSlot }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            {{ paymentStatusMeta[rescheduleSourceAppointment.paymentStatus]?.label || rescheduleSourceAppointment.paymentStatus }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="section-meta">
          <span>请选择新的号源</span>
          <span v-if="scheduleLoading">排班加载中...</span>
        </div>

        <el-table :data="rescheduleCandidateSchedules" border>
          <el-table-column prop="department" label="科室" width="120" />
          <el-table-column prop="doctorName" label="医生" width="100" />
          <el-table-column prop="title" label="职称" width="120" />
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="timeSlot" label="时段" width="130" />
          <el-table-column label="挂号费" width="100">
            <template #default="{ row }">{{ formatFee(row.fee) }}</template>
          </el-table-column>
          <el-table-column label="余号" width="90">
            <template #default="{ row }">{{ row.remainingSlots }}</template>
          </el-table-column>
          <el-table-column label="选择" width="90" fixed="right">
            <template #default="{ row }">
              <el-radio v-model="rescheduleTargetScheduleId" :label="row.id">选择</el-radio>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!scheduleLoading && rescheduleCandidateSchedules.length === 0" description="当前筛选条件下暂无可改约号源" />
      </div>
      <template #footer>
        <el-button @click="closeRescheduleDialog">取消</el-button>
        <el-button type="primary" :loading="rescheduleSubmitting" @click="confirmReschedule">确认改约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  cancelAppointment,
  createAppointment,
  fetchMyAppointments,
  fetchSchedules,
  payAppointment,
  rescheduleAppointment,
} from "../../services/patientAppointments";
import { getActivePatient } from "../../services/patientSession";

const departments = ["心内科", "呼吸内科", "神经内科", "骨科"];
const defaultDate = new Date().toISOString().slice(0, 10);

const appointmentStatusMeta = {
  BOOKED: { label: "已预约", type: "primary" },
  CANCELLED: { label: "已取消", type: "info" },
  RESCHEDULED: { label: "已改约", type: "warning" },
};

const paymentStatusMeta = {
  UNPAID: { label: "待支付", type: "warning" },
  PAID: { label: "已支付", type: "success" },
  REFUNDED: { label: "已退款", type: "info" },
};

const activePatient = ref(getActivePatient());
const scheduleQuery = ref({
  department: "",
  date: defaultDate,
});
const appointmentFilter = ref({
  status: "",
  paymentStatus: "",
});
const schedules = ref([]);
const myAppointments = ref([]);
const selectedSchedule = ref(null);
const bookingDialogVisible = ref(false);
const rescheduleDialogVisible = ref(false);
const rescheduleSourceAppointment = ref(null);
const rescheduleTargetScheduleId = ref("");
const scheduleLoading = ref(false);
const appointmentLoading = ref(false);
const bookingSubmitting = ref(false);
const rescheduleSubmitting = ref(false);
const paySubmittingId = ref("");

const filteredAppointments = computed(() => {
  return myAppointments.value.filter((item) => {
    if (appointmentFilter.value.status && item.status !== appointmentFilter.value.status) {
      return false;
    }
    if (appointmentFilter.value.paymentStatus && item.paymentStatus !== appointmentFilter.value.paymentStatus) {
      return false;
    }
    return true;
  });
});

const rescheduleCandidateSchedules = computed(() => {
  if (!rescheduleSourceAppointment.value) {
    return [];
  }
  return schedules.value.filter(
    (item) => item.id !== rescheduleSourceAppointment.value.scheduleId && item.remainingSlots > 0
  );
});

function formatFee(fee) {
  return `${Number(fee || 0).toFixed(2)} 元`;
}

function scheduleTagType(remainingSlots) {
  if (remainingSlots <= 0) {
    return "danger";
  }
  if (remainingSlots <= 1) {
    return "warning";
  }
  return "success";
}

async function loadSchedules() {
  scheduleLoading.value = true;
  try {
    schedules.value = await fetchSchedules(scheduleQuery.value);
  } catch (error) {
    ElMessage.error(error.message || "排班加载失败");
  } finally {
    scheduleLoading.value = false;
  }
}

async function loadMyAppointments() {
  appointmentLoading.value = true;
  activePatient.value = getActivePatient();
  try {
    myAppointments.value = await fetchMyAppointments();
  } catch (error) {
    ElMessage.error(error.message || "预约记录加载失败");
  } finally {
    appointmentLoading.value = false;
  }
}

async function refreshAll() {
  await Promise.all([loadSchedules(), loadMyAppointments()]);
}

function resetScheduleQuery() {
  scheduleQuery.value = {
    department: "",
    date: defaultDate,
  };
  loadSchedules();
}

function resetAppointmentFilter() {
  appointmentFilter.value = {
    status: "",
    paymentStatus: "",
  };
}

function openBookingDialog(schedule) {
  selectedSchedule.value = schedule;
  bookingDialogVisible.value = true;
}

function closeRescheduleDialog() {
  rescheduleDialogVisible.value = false;
  rescheduleSourceAppointment.value = null;
  rescheduleTargetScheduleId.value = "";
}

async function openRescheduleDialog(row) {
  rescheduleSourceAppointment.value = row;
  rescheduleTargetScheduleId.value = "";
  rescheduleDialogVisible.value = true;
  if (schedules.value.length === 0) {
    await loadSchedules();
  }
}

async function confirmBooking() {
  if (!selectedSchedule.value) {
    return;
  }

  bookingSubmitting.value = true;
  try {
    await createAppointment({
      scheduleId: selectedSchedule.value.id,
    });
    ElMessage.success("预约创建成功，当前为待支付状态");
    bookingDialogVisible.value = false;
    selectedSchedule.value = null;
    await refreshAll();
  } catch (error) {
    ElMessage.error(error.message || "预约失败");
  } finally {
    bookingSubmitting.value = false;
  }
}

async function handlePay(row) {
  paySubmittingId.value = row.id;
  try {
    await ElMessageBox.confirm(`确认对预约单 ${row.serialNumber} 执行虚拟支付吗？`, "虚拟支付", { type: "warning" });
    await payAppointment(row.id);
    ElMessage.success("支付成功");
    await refreshAll();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "支付失败");
    }
  } finally {
    paySubmittingId.value = "";
  }
}

async function confirmReschedule() {
  if (!rescheduleSourceAppointment.value) {
    return;
  }
  if (!rescheduleTargetScheduleId.value) {
    ElMessage.warning("请选择新的号源");
    return;
  }

  rescheduleSubmitting.value = true;
  try {
    await rescheduleAppointment(rescheduleSourceAppointment.value.id, {
      targetScheduleId: rescheduleTargetScheduleId.value,
    });
    ElMessage.success("改约成功，已生成新的待支付预约");
    closeRescheduleDialog();
    await refreshAll();
  } catch (error) {
    ElMessage.error(error.message || "改约失败");
  } finally {
    rescheduleSubmitting.value = false;
  }
}

async function handleCancel(row) {
  try {
    const refundHint = row.paymentStatus === "PAID" ? "，本次会标记为已退款" : "";
    await ElMessageBox.confirm(
      `确认取消 ${row.department} ${row.doctorName} ${row.date} ${row.timeSlot} 的预约吗${refundHint}？`,
      "取消预约",
      { type: "warning" }
    );
    await cancelAppointment(row.id);
    ElMessage.success(row.paymentStatus === "PAID" ? "预约已取消并标记退款" : "预约已取消");
    await refreshAll();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "取消预约失败");
    }
  }
}

onMounted(() => {
  refreshAll();
});
</script>

<style scoped>
.appointment-page {
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

.filter-form {
  margin-bottom: 8px;
}

.compact-filter {
  margin-bottom: 4px;
}

.section-meta {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}

.action-group {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.reschedule-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reschedule-alert,
.reschedule-summary {
  margin-bottom: 4px;
}

.muted-text {
  color: #94a3b8;
}

@media (max-width: 900px) {
  .header-row,
  .section-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
