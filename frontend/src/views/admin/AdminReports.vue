<template>
  <div class="admin-reports-page">
    <el-card shadow="never" class="hero-card">
      <div class="header-row">
        <div>
          <div class="panel-title">运营报表</div>
          <div class="panel-subtitle">基于真实排班、预约、接诊和患者数据生成的简版运营统计。</div>
        </div>
        <el-button @click="loadReports">刷新数据</el-button>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col v-for="item in reportModel.cards" :key="item.label" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card" v-loading="loading">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-12" shadow="never" v-loading="loading">
      <template #header>
        <span>真实统计表</span>
      </template>
      <el-table :data="reportModel.table" border>
        <el-table-column prop="metric" label="指标" min-width="220" />
        <el-table-column prop="value" label="当前值" width="140" />
      </el-table>
      <el-empty v-if="!loading && reportModel.table.length === 0" description="暂无可展示统计数据" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { buildAdminOperationsReportModel, fetchAdminOperationsReport } from "../../services/adminReports.js";

const loading = ref(false);
const reportModel = ref(buildAdminOperationsReportModel());

async function loadReports() {
  loading.value = true;
  try {
    const report = await fetchAdminOperationsReport();
    reportModel.value = buildAdminOperationsReportModel(report);
  } catch (error) {
    reportModel.value = buildAdminOperationsReportModel();
    ElMessage.error(error.message || "运营报表加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadReports();
});
</script>

<style scoped>
.admin-reports-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #fde68a;
  background: linear-gradient(135deg, #fff7ed 0%, #ffffff 60%, #fffbeb 100%);
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
  color: #b45309;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #78716c;
}

.mt-12 {
  margin-top: 12px;
}

.stat-card {
  min-height: 138px;
}

.stat-label {
  color: #5f706c;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  color: #b45309;
  font-weight: 700;
}

.stat-desc {
  margin-top: 6px;
  color: #7b8d89;
  font-size: 12px;
}

@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>