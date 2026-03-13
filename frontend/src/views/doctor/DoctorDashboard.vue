<template>
  <div class="doctor-dashboard-page doctor-command-panel">
    <el-card shadow="never" class="hero-card">
      <div class="hero-row">
        <div class="hero-copy-block">
          <p class="hero-eyebrow">Doctor Command</p>
          <div class="hero-title">医生工作总览</div>
          <div class="hero-subtitle">围绕当前候诊、接诊和患者维护情况生成的真实工作中枢。</div>
          <div class="hero-badges">
            <span class="hero-badge">今日接诊面板</span>
            <span class="hero-badge hero-badge-soft">效率优先</span>
          </div>
        </div>

        <div class="hero-actions">
          <el-button class="refresh-button" @click="loadDashboard">刷新</el-button>
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
        <el-card shadow="never" class="section-card" v-loading="loading">
          <template #header>
            <div class="section-head">
              <div>
                <span class="section-title">今日候诊与接诊概览</span>
                <p class="section-subtitle">优先帮助医生确认当前患者状态和入口来源。</p>
              </div>
            </div>
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
        <el-card shadow="never" class="section-card todo-card">
          <template #header>
            <div class="section-head">
              <div>
                <span class="section-title">待处理事项</span>
                <p class="section-subtitle">把当天仍需关注的动作收在一个轻量工作清单里。</p>
              </div>
            </div>
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
.doctor-command-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-family: "Segoe UI Variable Text", "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

.hero-card,
.section-card,
.stat-card {
  border: 1px solid rgba(125, 164, 190, 0.2);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.06);
}

.hero-card {
  background:
    radial-gradient(circle at 92% 16%, rgba(56, 189, 248, 0.2), transparent 24%),
    linear-gradient(135deg, #eef6ff 0%, #f8fafc 58%, #eefaf7 100%);
}

.hero-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.hero-copy-block {
  max-width: 640px;
}

.hero-eyebrow {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #0f766e;
}

.hero-title {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #5b6c77;
}

.hero-badges {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #0f4c81;
  background: rgba(37, 99, 235, 0.1);
}

.hero-badge-soft {
  color: #0f766e;
  background: rgba(15, 118, 110, 0.1);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.refresh-button {
  min-height: 42px;
  border-radius: 14px;
}

.mt-12 {
  margin-top: 12px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.84);
}

.stat-card .label {
  color: #5e706c;
  font-size: 13px;
}

.stat-card .value {
  font-size: 30px;
  color: #2563eb;
  margin-top: 10px;
  font-weight: 700;
}

.stat-card .desc {
  color: #6f8189;
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.6;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #16344f;
}

.section-subtitle {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: #64748b;
}

.todo-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(240, 249, 255, 0.9));
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

@media (max-width: 900px) {
  .hero-row,
  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
