<template>
  <div class="admin-billing-page">
    <el-card shadow="never" class="hero-card">
      <div class="header-row">
        <div>
          <div class="panel-title">收费与医保</div>
        </div>
        <el-button @click="loadBilling">刷新数据</el-button>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col v-for="item in billingModel.cards" :key="item.label" :xs="24" :sm="12" :lg="8">
        <el-card shadow="never" class="stat-card" v-loading="loading">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" v-loading="loading" class="mt-12">
      <template #header>
        <span>{{ '\u771f\u5b9e\u8d26\u5355\u8bb0\u5f55' }}</span>
      </template>
      <el-table :data="billingModel.bills" border>
        <el-table-column prop="serialNumber" :label="'\u8d26\u5355\u53f7'" min-width="180" />
        <el-table-column prop="patientLabel" :label="'\u60a3\u8005'" min-width="160" />
        <el-table-column prop="department" :label="'\u79d1\u5ba4'" width="110" />
        <el-table-column prop="doctorName" :label="'\u533b\u751f'" width="110" />
        <el-table-column prop="visitTime" :label="'\u5c31\u8bca\u65f6\u95f4'" min-width="170" />
        <el-table-column prop="amountLabel" :label="'\u91d1\u989d'" width="110" />
        <el-table-column :label="'\u4e1a\u52a1\u72b6\u6001'" width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="'\u652f\u4ed8\u72b6\u6001'" width="100">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatusType">{{ row.paymentStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paidAtLabel" :label="'\u652f\u4ed8\u65f6\u95f4'" min-width="170" />
      </el-table>
      <el-empty v-if="!loading && billingModel.bills.length === 0" :description="billingModel.emptyHint" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { buildAdminBillingModel, fetchAdminBillingOverview } from "../../services/adminBilling.js";

const loading = ref(false);
const billingModel = ref(buildAdminBillingModel());

async function loadBilling() {
  loading.value = true;
  try {
    const overview = await fetchAdminBillingOverview();
    billingModel.value = buildAdminBillingModel(overview);
  } catch (error) {
    billingModel.value = buildAdminBillingModel();
    ElMessage.error(error.message || "收费总览加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadBilling();
});
</script>

<style scoped>
.admin-billing-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #fed7aa;
  background: linear-gradient(135deg, #fff7ed 0%, #ffffff 58%, #fffbeb 100%);
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
  color: #c2410c;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #78716c;
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
  color: #c2410c;
  font-weight: 700;
}

.stat-desc {
  margin-top: 6px;
  color: #7b8d89;
  font-size: 12px;
}

.mt-12 {
  margin-top: 12px;
}


@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>