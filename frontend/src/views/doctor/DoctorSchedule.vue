<template>
  <div class="doctor-schedule-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">我的排班</div>
            <div class="panel-subtitle">只读查看当前医生账号已绑定的排班信息。</div>
          </div>
          <el-button @click="loadSchedules">刷新</el-button>
        </div>
      </template>

      <el-alert
        title="排班由管理端维护，医生端仅提供查看。"
        type="info"
        :closable="false"
        show-icon
        class="page-alert"
      />

      <div class="insight-grid">
        <div
          v-for="item in scheduleModel.summaryCards"
          :key="item.label"
          class="insight-card"
          :class="{ 'is-textual': typeof item.value === 'string' && String(item.value).length > 8 }"
        >
          <div class="insight-label">{{ item.label }}</div>
          <div class="insight-value">{{ item.value }}</div>
          <div class="insight-hint">{{ item.hint }}</div>
        </div>
      </div>

      <div class="section-meta">
        <span>{{ scheduleModel.items.length }} 条排班记录</span>
        <span v-if="loading">加载中...</span>
      </div>

      <el-table :data="scheduleModel.items" border v-loading="loading">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="weekdayLabel" label="星期" width="90" />
        <el-table-column prop="timeSlot" label="时段" width="130" />
        <el-table-column prop="department" label="科室" min-width="120" />
        <el-table-column prop="doctorName" label="医生" min-width="120" />
        <el-table-column prop="title" label="职称" min-width="120" />
        <el-table-column prop="feeLabel" label="费用" width="110" />
        <el-table-column prop="quotaLabel" label="号源" width="130" />
        <el-table-column label="状态" width="100">
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
    ElMessage.error(error.message || "医生排班加载失败");
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

.insight-grid {
  margin-bottom: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.insight-card {
  padding: 14px 16px;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
}

.insight-card.is-textual .insight-value {
  font-size: 18px;
}

.insight-label {
  font-size: 12px;
  color: #64748b;
}

.insight-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #164e63;
  line-height: 1.2;
  word-break: break-word;
}

.insight-hint {
  margin-top: 6px;
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

@media (max-width: 1100px) {
  .insight-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .header-row,
  .section-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>