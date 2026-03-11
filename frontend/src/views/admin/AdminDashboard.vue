<template>
  <div class="admin-dashboard-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-row">
        <div>
          <div class="hero-title">管理端运营总览</div>
          <div class="hero-subtitle">基于真实排班、预约、接诊和患者数据生成的最小运营视图。</div>
        </div>
        <div class="hero-actions">
          <el-button @click="loadDashboard">刷新</el-button>
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
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>运营概览</span>
          </template>
          <el-table :data="dashboardModel.overview" border>
            <el-table-column prop="metric" label="指标" width="180" />
            <el-table-column prop="today" label="当前值" width="120" />
            <el-table-column prop="desc" label="说明" min-width="220" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>管理提醒</span>
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
.admin-dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #fde68a;
  background: linear-gradient(135deg, #fffbeb 0%, #ffffff 55%, #fff7ed 100%);
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
  color: #b45309;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #78716c;
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

.mb-8 {
  margin-bottom: 8px;
}

.label {
  color: #5f706c;
  font-size: 13px;
}

.value {
  margin-top: 8px;
  font-size: 26px;
  color: #b45309;
  font-weight: 700;
}

.trend {
  margin-top: 6px;
  color: #7b8d89;
  font-size: 12px;
  line-height: 1.5;
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