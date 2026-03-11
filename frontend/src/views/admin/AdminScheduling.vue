<template>
  <div class="scheduling-page">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">Schedule Config</div>
                <div class="panel-subtitle">Bind schedules to real doctor accounts instead of free-text names.</div>
              </div>
              <div class="header-actions">
                <el-button @click="loadSchedules">Refresh</el-button>
                <el-button type="primary" @click="openCreateDialog">New Schedule</el-button>
              </div>
            </div>
          </template>

          <div class="section-meta">
            <span>{{ schedules.length }} schedule records</span>
            <span v-if="loading">Loading...</span>
          </div>

          <el-table :data="schedules" border v-loading="loading">
            <el-table-column prop="doctorName" label="Doctor" min-width="120" />
            <el-table-column prop="doctorUsername" label="Account" min-width="120" />
            <el-table-column prop="title" label="Title" min-width="120" />
            <el-table-column prop="department" label="Department" min-width="120" />
            <el-table-column prop="date" label="Date" width="120" />
            <el-table-column prop="timeSlot" label="Time" width="130" />
            <el-table-column label="Fee" width="100">
              <template #default="{ row }">{{ formatFee(row.fee) }}</template>
            </el-table-column>
            <el-table-column prop="totalSlots" label="Total" width="90" />
            <el-table-column prop="remainingSlots" label="Left" width="90" />
            <el-table-column label="Status" width="100">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">
                  {{ row.enabled ? 'Enabled' : 'Disabled' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions" min-width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEditDialog(row)">Edit</el-button>
                <el-button type="danger" link :disabled="!row.enabled" @click="handleDisable(row)">Disable</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>
            <span>Binding Rules</span>
          </template>
          <el-alert
            title="Schedules now snapshot doctor name, title, and department from the selected doctor account."
            type="success"
            :closable="false"
            show-icon
          />
          <el-descriptions :column="1" border class="rule-box">
            <el-descriptions-item label="Source">Only enabled doctor accounts are selectable.</el-descriptions-item>
            <el-descriptions-item label="Patient View">Patients only see enabled schedules.</el-descriptions-item>
            <el-descriptions-item label="Doctor View">Doctors query schedules by username binding.</el-descriptions-item>
            <el-descriptions-item label="Editing">Total slots cannot drop below used slots.</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? 'New Schedule' : 'Edit Schedule'" width="560px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="Doctor">
          <el-select
            v-model="form.doctorUsername"
            filterable
            placeholder="Select a doctor account"
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
        <el-form-item label="Doctor Name">
          <el-input v-model="form.doctorName" disabled />
        </el-form-item>
        <el-form-item label="Title">
          <el-input v-model="form.title" disabled />
        </el-form-item>
        <el-form-item label="Department">
          <el-input v-model="form.department" disabled />
        </el-form-item>
        <el-form-item label="Date">
          <el-date-picker
            v-model="form.date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="Select a date"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="Time Slot">
          <el-input v-model="form.timeSlot" placeholder="e.g. 09:00-09:30" />
        </el-form-item>
        <el-form-item label="Fee">
          <el-input-number v-model="form.fee" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Total Slots">
          <el-input-number v-model="form.totalSlots" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSchedule">Save</el-button>
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
  return `${doctor.displayName} (${doctor.username}) / ${doctor.department || 'No department'} / ${doctor.title || 'No title'}`;
}

function formatFee(fee) {
  return `${Number(fee || 0).toFixed(2)} CNY`;
}

async function loadDoctorOptions() {
  doctorOptionsLoading.value = true;
  try {
    doctorOptions.value = await fetchScheduleDoctorOptions();
  } catch (error) {
    ElMessage.error(error.message || "Failed to load doctor accounts");
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
    ElMessage.error(error.message || "Failed to load schedules");
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
    ElMessage.warning("Please complete the schedule form");
    return false;
  }
  if (Number(form.totalSlots) <= 0) {
    ElMessage.warning("Total slots must be greater than 0");
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
      ElMessage.success("Schedule created");
    } else {
      await updateAdminSchedule(editingScheduleId.value, payload);
      ElMessage.success("Schedule updated");
    }
    dialogVisible.value = false;
    await loadSchedules();
  } catch (error) {
    ElMessage.error(error.message || "Failed to save schedule");
  } finally {
    submitting.value = false;
  }
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm(
      `Disable ${row.department} / ${row.doctorName} / ${row.date} ${row.timeSlot}?`,
      "Disable Schedule",
      { type: "warning" }
    );
    await disableAdminSchedule(row.id);
    ElMessage.success("Schedule disabled");
    await loadSchedules();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "Failed to disable schedule");
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