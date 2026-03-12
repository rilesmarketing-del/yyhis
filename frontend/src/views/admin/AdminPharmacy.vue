<template>
  <div class="admin-pharmacy-page">
    <el-card shadow="never" class="hero-card">
      <div class="header-row">
        <div>
          <div class="panel-title">药房与库存</div>
          <div class="panel-subtitle">基于真实接诊记录里的结构化处方条目和文字处方内容生成的最小药房总览。</div>
        </div>
        <el-button @click="loadPharmacy">刷新数据</el-button>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col v-for="item in pharmacyModel.cards" :key="item.label" :xs="24" :sm="12" :lg="8">
        <el-card shadow="never" class="stat-card" v-loading="loading">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="17">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>处方记录</span>
          </template>
          <el-table :data="pharmacyModel.records" border>
            <el-table-column prop="id" label="记录编号" min-width="180" />
            <el-table-column prop="patientLabel" label="患者" min-width="150" />
            <el-table-column prop="department" label="科室" min-width="120" />
            <el-table-column prop="doctorName" label="医生" min-width="120" />
            <el-table-column prop="visitTime" label="就诊时间" min-width="160" />
            <el-table-column label="接诊状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.statusType" effect="plain">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="structuredCount" label="条目数" width="90" />
            <el-table-column prop="prescriptionPreview" label="处方摘要" min-width="240" show-overflow-tooltip />
          </el-table>
          <el-empty v-if="!loading && pharmacyModel.records.length === 0" :description="pharmacyModel.emptyHint" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="note-card" v-loading="loading">
          <template #header>
            <span>能力说明</span>
          </template>
          <div class="note-text">{{ pharmacyModel.note }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { buildAdminPharmacyModel, fetchAdminPharmacyOverview } from "../../services/adminPharmacy.js";

const loading = ref(false);
const pharmacyModel = ref(buildAdminPharmacyModel());

async function loadPharmacy() {
  loading.value = true;
  try {
    const overview = await fetchAdminPharmacyOverview();
    pharmacyModel.value = buildAdminPharmacyModel(overview);
  } catch (error) {
    pharmacyModel.value = buildAdminPharmacyModel();
    ElMessage.error(error.message || "药房总览加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadPharmacy();
});
</script>

<style scoped>
.admin-pharmacy-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #a7f3d0;
  background: linear-gradient(135deg, #ecfdf5 0%, #ffffff 55%, #f0fdf4 100%);
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
  color: #047857;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #5f7a72;
}

.mt-12 {
  margin-top: 12px;
}

.stat-card {
  min-height: 136px;
}

.stat-label {
  color: #4b635c;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  color: #047857;
  font-weight: 700;
}

.stat-desc {
  margin-top: 6px;
  color: #6b7f78;
  font-size: 12px;
  line-height: 1.6;
}

.note-card {
  height: 100%;
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