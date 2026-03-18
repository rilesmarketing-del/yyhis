<template>
  <div class="admin-system-page">
    <el-card shadow="never" class="hero-card">
      <div class="header-row">
        <div>
          <div class="panel-title">系统管理</div>
        </div>
        <el-button @click="loadSystemOverview">刷新数据</el-button>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col v-for="item in systemModel.cards" :key="item.label" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card" v-loading="loading">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" v-loading="loading" class="section-card">
          <template #header>
            <span>角色账号分布</span>
          </template>
          <div v-if="systemModel.roleStats.length > 0" class="role-stats">
            <div v-for="item in systemModel.roleStats" :key="item.role" class="role-stat-item">
              <div class="role-stat-header">
                <el-tag :type="item.tagType" effect="plain">{{ item.label }}</el-tag>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
          <el-empty v-else :description="systemModel.emptyRoleHint" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="15">
        <el-card shadow="never" v-loading="loading" class="section-card">
          <template #header>
            <span>关键运营指标</span>
          </template>
          <el-table :data="systemModel.metrics" border>
            <el-table-column prop="metric" label="指标" min-width="220" />
            <el-table-column prop="value" label="当前值" width="140" />
          </el-table>
          <el-empty v-if="!loading && systemModel.metrics.length === 0" :description="systemModel.emptyMetricsHint" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" v-loading="loading" class="section-card">
          <template #header>
            <span>系统提醒</span>
          </template>
          <el-alert
            v-for="item in systemModel.reminders"
            :key="item.id"
            :title="item.title"
            :type="item.type"
            :closable="false"
            show-icon
            class="mb-8"
          />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" v-loading="loading" class="note-card">
          <template #header>
            <span>阶段说明</span>
          </template>
          <div class="note-text">{{ systemModel.note }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { buildAdminSystemModel, fetchAdminSystemOverview } from "../../services/adminSystem.js";

const loading = ref(false);
const systemModel = ref(buildAdminSystemModel());

async function loadSystemOverview() {
  loading.value = true;
  try {
    const overview = await fetchAdminSystemOverview();
    systemModel.value = buildAdminSystemModel(overview);
  } catch (error) {
    systemModel.value = buildAdminSystemModel();
    ElMessage.error(error.message || "系统运行总览加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadSystemOverview();
});
</script>

<style scoped>
.admin-system-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #bfdbfe;
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 58%, #f8fafc 100%);
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
  color: #1d4ed8;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.mt-12 {
  margin-top: 12px;
}

.mb-8 {
  margin-bottom: 8px;
}

.stat-card {
  min-height: 136px;
}

.stat-label {
  color: #4f647e;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  color: #1d4ed8;
  font-weight: 700;
}

.stat-desc {
  margin-top: 6px;
  color: #6b7c93;
  font-size: 12px;
  line-height: 1.6;
}

.section-card,
.note-card {
  height: 100%;
}

.role-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.role-stat-item {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: #f8fbff;
}

.role-stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.note-text {
  color: #475569;
  line-height: 1.8;
  font-size: 13px;
}

@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>