<template>
  <div class="admin-billing-page">
    <el-card shadow="never" class="hero-card">
      <div class="header-row">
        <div>
          <div class="panel-title">收费与医保</div>
          <div class="panel-subtitle">基于真实挂号预约收费记录生成的最小收费总览。</div>
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

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="17">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>真实账单记录</span>
          </template>
          <el-table :data="billingModel.bills" border>
            <el-table-column prop="serialNumber" label="账单号" min-width="180" />
            <el-table-column prop="patientLabel" label="患者" min-width="160" />
            <el-table-column prop="department" label="科室" width="110" />
            <el-table-column prop="doctorName" label="医生" width="110" />
            <el-table-column prop="visitTime" label="就诊时间" min-width="170" />
            <el-table-column prop="amountLabel" label="金额" width="110" />
            <el-table-column label="业务状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.statusType">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="支付状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.paymentStatusType">{{ row.paymentStatusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paidAtLabel" label="支付时间" min-width="170" />
          </el-table>
          <el-empty v-if="!loading && billingModel.bills.length === 0" :description="billingModel.emptyHint" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="note-card" v-loading="loading">
          <template #header>
            <span>收费说明</span>
          </template>
          <el-alert
            title="当前收费视图基于挂号预约收费记录。"
            type="info"
            :closable="false"
            show-icon
          />
          <el-descriptions :column="1" border class="note-box">
            <el-descriptions-item label="当前范围">包含预约产生的待支付、已支付和已退款记录。</el-descriptions-item>
            <el-descriptions-item label="暂未纳入">检查、药品、住院和发票相关收费暂未纳入本页。</el-descriptions-item>
            <el-descriptions-item label="用途">用于管理端快速查看真实收费流转，而非人工处理入口。</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
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

.note-card {
  min-height: 100%;
}

.note-box {
  margin-top: 14px;
}

@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>