<template>
  <div class="scheduling-page">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">排班配置</div>
                <div class="panel-subtitle">排班直接绑定真实医生账号，不再使用自由填写的医生姓名。</div>
              </div>
              <div class="header-actions">
                <el-button @click="loadSchedules">刷新</el-button>
                <el-button type="primary" @click="openCreateDialog">新建排班</el-button>
              </div>
            </div>
          </template>

          <div class="section-meta">
            <span>{{ schedules.length }} 条排班记录</span>
            <span v-if="loading">加载中...</span>
          </div>

          <el-table :data="schedules" border v-loading="loading">
            <el-table-column prop="doctorName" label="医生" min-width="120" />
            <el-table-column prop="doctorUsername" label="账号" min-width="120" />
            <el-table-column prop="title" label="职称" min-width="120" />
            <el-table-column prop="department" label="科室" min-width="120" />
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="timeSlot" label="时段" width="130" />
            <el-table-column label="费用" width="100">
              <template #default="{ row }">{{ formatFee(row.fee) }}</template>
            </el-table-column>
            <el-table-column prop="totalSlots" label="总号源" width="90" />
            <el-table-column prop="remainingSlots" label="剩余" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">
                  {{ row.enabled ? '已启用' : '已停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
                <el-button type="danger" link :disabled="!row.enabled" @click="handleDisable(row)">停用</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>
            <span>绑定规则</span>
          </template>
          <el-alert
            title="排班会从所选医生账号同步医生姓名、职称和科室快照。"
            type="success"
            :closable="false"
            show-icon
          />
          <el-descriptions :column="1" border class="rule-box">
            <el-descriptions-item label="来源">只能选择已启用的医生账号。</el-descriptions-item>
            <el-descriptions-item label="患者端">患者只能看到已启用的排班。</el-descriptions-item>
            <el-descriptions-item label="医生端">医生按账号绑定关系查询自己的排班。</el-descriptions-item>
            <el-descriptions-item label="编辑限制">总号源不能小于已使用号源。</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建排班' : '编辑排班'" width="560px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="医生">
          <el-select
            v-model="form.doctorUsername"
            filterable
            placeholder="请选择医生账号"
            :loading="doctorOptionsLoading"
            style="width: 100%"
            @change="handleDoctorChange"
          >
            <el-option
              v-for="item in doctorOptions"
              :key="item.username"
              :label="formatDoctorOption(item)"
              :value="item.username"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生姓名">
          <el-input v-model="form.doctorName" disabled />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" disabled />
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="form.department" disabled />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="form.date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时段">
          <el-input v-model="form.timeSlot" placeholder="例如 09:00-09:30" />
        </el-form-item>
        <el-form-item label="费用">
          <el-input-number v-model="form.fee" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总号源">
          <el-input-number v-model="form.totalSlots" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSchedule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createAdminSchedule,
  createScheduleDraftFromDoctor,
  disableAdminSchedule,
  fetchAdminSchedules,
  fetchScheduleDoctorOptions,
  updateAdminSchedule,
} from "../../services/adminSchedules.js";

const loading = ref(false);
const doctorOptionsLoading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref("create");
const editingScheduleId = ref("");
const schedules = ref([]);
const doctorOptions = ref([]);
const form = reactive(createEmptyForm());

function createEmptyForm() {
  return {
    doctorUsername: "",
    doctorName: "",
    title: "",
    department: "",
    date: "",
    timeSlot: "",
    fee: 20,
    totalSlots: 10,
  };
}

function fillForm(payload = {}) {
  form.doctorUsername = payload.doctorUsername || "";
  form.doctorName = payload.doctorName || "";
  form.title = payload.title || "";
  form.department = payload.department || "";
  form.date = payload.date || "";
  form.timeSlot = payload.timeSlot || "";
  form.fee = Number(payload.fee ?? 20);
  form.totalSlots = Number(payload.totalSlots ?? 10);
}

function syncDoctorSnapshot(username) {
  const doctor = doctorOptions.value.find((item) => item.username === username);
  const draft = createScheduleDraftFromDoctor(doctor);
  form.doctorName = draft.doctorName;
  form.title = draft.title;
  form.department = draft.department;
}

function formatDoctorOption(doctor) {
  return `${doctor.displayName} (${doctor.username}) / ${doctor.department || '未设置科室'} / ${doctor.title || '未设置职称'}`;
}

function formatFee(fee) {
  return `${Number(fee || 0).toFixed(2)} 元`;
}

async function loadDoctorOptions() {
  doctorOptionsLoading.value = true;
  try {
    doctorOptions.value = await fetchScheduleDoctorOptions();
  } catch (error) {
    ElMessage.error(error.message || "医生账号加载失败");
  } finally {
    doctorOptionsLoading.value = false;
  }
}

async function ensureDoctorOptions() {
  if (doctorOptions.value.length > 0) {
    return;
  }
  await loadDoctorOptions();
}

async function loadSchedules() {
  loading.value = true;
  try {
    schedules.value = await fetchAdminSchedules();
  } catch (error) {
    ElMessage.error(error.message || "排班加载失败");
  } finally {
    loading.value = false;
  }
}

async function openCreateDialog() {
  await ensureDoctorOptions();
  dialogMode.value = "create";
  editingScheduleId.value = "";
  fillForm(createEmptyForm());
  dialogVisible.value = true;
}

async function openEditDialog(row) {
  await ensureDoctorOptions();
  dialogMode.value = "edit";
  editingScheduleId.value = row.id;
  fillForm({
    ...row,
    doctorUsername: row.doctorUsername || resolveDoctorUsername(row),
  });
  if (form.doctorUsername) {
    syncDoctorSnapshot(form.doctorUsername);
  }
  dialogVisible.value = true;
}

function resolveDoctorUsername(row) {
  const matchedDoctor = doctorOptions.value.find(
    (item) =>
      item.displayName === row.doctorName &&
      item.title === row.title &&
      item.department === row.department
  );
  return matchedDoctor?.username || "";
}

function handleDoctorChange(username) {
  syncDoctorSnapshot(username);
}

function buildPayload() {
  return {
    doctorUsername: form.doctorUsername,
    date: form.date,
    timeSlot: form.timeSlot.trim(),
    fee: Number(form.fee).toFixed(2),
    totalSlots: Number(form.totalSlots),
  };
}

function validateForm() {
  if (!form.doctorUsername || !form.date || !form.timeSlot.trim()) {
    ElMessage.warning("请完整填写排班信息");
    return false;
  }
  if (Number(form.totalSlots) <= 0) {
    ElMessage.warning("总号源必须大于 0");
    return false;
  }
  return true;
}

async function submitSchedule() {
  if (!validateForm()) {
    return;
  }

  submitting.value = true;
  try {
    const payload = buildPayload();
    if (dialogMode.value === "create") {
      await createAdminSchedule(payload);
      ElMessage.success("排班创建成功");
    } else {
      await updateAdminSchedule(editingScheduleId.value, payload);
      ElMessage.success("排班更新成功");
    }
    dialogVisible.value = false;
    await loadSchedules();
  } catch (error) {
    ElMessage.error(error.message || "排班保存失败");
  } finally {
    submitting.value = false;
  }
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm(
      `确认停用 ${row.department} / ${row.doctorName} / ${row.date} ${row.timeSlot} 的排班吗？`,
      "停用排班",
      { type: "warning" }
    );
    await disableAdminSchedule(row.id);
    ElMessage.success("排班已停用");
    await loadSchedules();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "排班停用失败");
    }
  }
}

onMounted(async () => {
  await Promise.allSettled([loadDoctorOptions(), loadSchedules()]);
});
</script>

<style scoped>
.scheduling-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 10px;
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

.section-meta {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}

.rule-box {
  margin-top: 14px;
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