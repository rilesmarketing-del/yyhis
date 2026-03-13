<template>
  <div class="admin-dashboard-page admin-ops-panel">
    <el-card shadow="never" class="hero-card">
      <div class="hero-row">
        <div class="hero-copy-block">
          <p class="hero-eyebrow">Admin Operations</p>
          <div class="hero-title">管理端运营总览</div>
          <div class="hero-subtitle">基于真实排班、预约、接诊和患者数据生成的最小运营视图。</div>
          <div class="hero-badges">
            <span class="hero-badge">运营中枢</span>
            <span class="hero-badge hero-badge-soft">今日重点一屏可见</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button class="refresh-button" @click="loadDashboard">刷新</el-button>
          <el-button
            v-for="action in dashboardModel.quickActions"
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
      <el-col v-for="kpi in dashboardModel.stats" :key="kpi.label" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card" v-loading="loading">
          <div class="label">{{ kpi.label }}</div>
          <div class="value">{{ kpi.value }}</div>
          <div class="trend">{{ kpi.trend }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="section-card" v-loading="loading">
          <template #header>
            <div class="section-head">
              <div>
                <span class="section-title">运营概览</span>
                <p class="section-subtitle">帮助管理端快速判断排班、预约和接诊是否处于稳定状态。</p>
              </div>
            </div>
          </template>
          <el-table :data="dashboardModel.overview" border>
            <el-table-column prop="metric" label="指标" width="180" />
            <el-table-column prop="today" label="当前值" width="120" />
            <el-table-column prop="desc" label="说明" min-width="220" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="section-card alert-card" v-loading="loading">
          <template #header>
            <div class="section-head">
              <div>
                <span class="section-title">管理提醒</span>
                <p class="section-subtitle">把影响演示和日常运营的重点提示放在高亮区域。</p>
              </div>
            </div>
          </template>
          <el-alert
            v-for="item in dashboardModel.alerts"
            :key="item.id"
            :title="item.title"
            :type="item.type"
            :closable="false"
            show-icon
            class="mb-8"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { buildAdminDashboardModel, fetchAdminDashboardSummary } from "../../services/adminDashboard.js";

const router = useRouter();
const loading = ref(false);
const dashboardModel = ref(buildAdminDashboardModel());

function goTo(path) {
  router.push(path);
}

async function loadDashboard() {
  loading.value = true;
  try {
    const summary = await fetchAdminDashboardSummary();
    dashboardModel.value = buildAdminDashboardModel(summary);
  } catch (error) {
    dashboardModel.value = buildAdminDashboardModel();
    ElMessage.error(error.message || "管理首页加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadDashboard();
});
</script>

<style scoped>
.admin-ops-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-family: "Segoe UI Variable Text", "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

.hero-card,
.section-card,
.stat-card {
  border: 1px solid rgba(217, 166, 84, 0.22);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.06);
}

.hero-card {
  background:
    radial-gradient(circle at 90% 18%, rgba(45, 212, 191, 0.18), transparent 22%),
    linear-gradient(135deg, #fffbeb 0%, #ffffff 55%, #fff7ed 100%);
}

.hero-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.hero-copy-block {
  max-width: 650px;
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
  color: #6b7280;
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
  color: #a16207;
  background: rgba(245, 158, 11, 0.14);
}

.hero-badge-soft {
  color: #0f766e;
  background: rgba(15, 118, 110, 0.12);
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

.mb-8 {
  margin-bottom: 8px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
}

.label {
  color: #5f706c;
  font-size: 13px;
}

.value {
  margin-top: 10px;
  font-size: 30px;
  color: #b45309;
  font-weight: 700;
}

.trend {
  margin-top: 8px;
  color: #7b8d89;
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
  color: #5b3410;
}

.section-subtitle {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: #78716c;
}

.alert-card {
  background: linear-gradient(180deg, rgba(255, 251, 235, 0.92), rgba(255, 255, 255, 0.96));
}

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.alert-card .el-alert) {
  border-radius: 16px;
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
