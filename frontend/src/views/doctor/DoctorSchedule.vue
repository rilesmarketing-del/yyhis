<template>
  <div class="doctor-schedule-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">My Schedule</div>
            <div class="panel-subtitle">Read-only view of schedules assigned to the current doctor account.</div>
          </div>
          <el-button @click="loadSchedules">Refresh</el-button>
        </div>
      </template>

      <el-alert
        title="Schedules are managed by admins. Doctors can only view them here."
        type="info"
        :closable="false"
        show-icon
        class="page-alert"
      />

      <div class="section-meta">
        <span>{{ scheduleModel.items.length }} schedule records</span>
        <span v-if="loading">Loading...</span>
      </div>

      <el-table :data="scheduleModel.items" border v-loading="loading">
        <el-table-column prop="date" label="Date" width="120" />
        <el-table-column prop="weekdayLabel" label="Day" width="90" />
        <el-table-column prop="timeSlot" label="Time" width="130" />
        <el-table-column prop="department" label="Department" min-width="120" />
        <el-table-column prop="doctorName" label="Doctor" min-width="120" />
        <el-table-column prop="title" label="Title" min-width="120" />
        <el-table-column prop="feeLabel" label="Fee" width="110" />
        <el-table-column prop="quotaLabel" label="Quota" width="130" />
        <el-table-column label="Status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && scheduleModel.items.length === 0" :description="scheduleModel.emptyHint" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { buildDoctorScheduleModel, fetchMyDoctorSchedules } from "../../services/doctorSchedule.js";

const loading = ref(false);
const scheduleModel = ref(buildDoctorScheduleModel());

async function loadSchedules() {
  loading.value = true;
  try {
    const schedules = await fetchMyDoctorSchedules();
    scheduleModel.value = buildDoctorScheduleModel(schedules);
  } catch (error) {
    ElMessage.error(error.message || "Failed to load doctor schedules");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadSchedules();
});
</script>

<style scoped>
.doctor-schedule-page {
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

.page-alert {
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
}
</style>