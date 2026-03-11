<template>
  <div class="doctor-dashboard-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-row">
        <div>
          <div class="hero-title">医生工作总览</div>
          <div class="hero-subtitle">围绕当前候诊、接诊和患者维护情况生成的真实总览。</div>
        </div>
        <div class="hero-actions">
          <el-button @click="loadDashboard">刷新</el-button>
          <el-button
            v-for="action in quickActions"
            :key="action.path"
            :type="action.type === 'info' ? '' : action.type"
            :plain="action.type !== 'primary'"
            @click="goTo(action.path)"
          >
            {{ action.label }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col v-for="item in stats" :key="item.label" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card" v-loading="loading">
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
          <div class="desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <span>今日候诊与接诊概览</span>
          </template>
          <el-table :data="overview" border v-loading="loading">
            <el-table-column prop="patientName" label="患者" width="120" />
            <el-table-column prop="department" label="科室" width="120" />
            <el-table-column prop="time" label="时间" min-width="160" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" width="110" />
          </el-table>
          <el-empty v-if="!loading && overview.length === 0" description="今天暂无候诊或接诊数据" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>
            <span>待处理事项</span>
          </template>
          <el-timeline>
            <el-timeline-item v-for="todo in todos" :key="todo.id" :type="todo.type">
              {{ todo.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchDoctorPatients, fetchDoctorQueue, fetchDoctorRecords } from "../../services/doctorClinic.js";
import { buildDoctorDashboardModel } from "../../services/doctorDashboard.js";

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

const router = useRouter();
const loading = ref(false);
const stats = ref([]);
const overview = ref([]);
const todos = ref([]);
const quickActions = ref([]);

function goTo(path) {
  router.push(path);
}

function statusTagType(status) {
  if (status === "待接诊") {
    return "warning";
  }
  if (status === "接诊中") {
    return "primary";
  }
  if (status === "已完成") {
    return "success";
  }
  return "info";
}

async function loadDashboard() {
  loading.value = true;
  try {
    const [queue, records, patients] = await Promise.all([fetchDoctorQueue(), fetchDoctorRecords(), fetchDoctorPatients()]);
    const model = buildDoctorDashboardModel({ queue, records, patients, today: getToday() });
    stats.value = model.stats;
    overview.value = model.overview;
    todos.value = model.todos;
    quickActions.value = model.quickActions;
  } catch (error) {
    ElMessage.error(error.message || "医生首页加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadDashboard();
});
</script>

<style scoped>
.doctor-dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
}

.hero-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  color: #1d4ed8;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.mt-12 {
  margin-top: 12px;
}

.stat-card .label {
  color: #5e706c;
  font-size: 13px;
}

.stat-card .value {
  font-size: 28px;
  color: #2563eb;
  margin-top: 8px;
  font-weight: 700;
}

.stat-card .desc {
  color: #7a8b87;
  margin-top: 6px;
  font-size: 12px;
}

@media (max-width: 900px) {
  .hero-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
