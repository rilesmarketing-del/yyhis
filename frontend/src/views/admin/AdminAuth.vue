<template>
  <div class="admin-auth-page">
    <el-card shadow="never" class="hero-card">
      <div class="hero-row">
        <div>
          <div class="hero-title">账号与角色查看</div>
          <div class="hero-subtitle">基于真实演示账号生成的最小只读权限概览，便于管理端查看当前角色边界。</div>
        </div>
        <el-button @click="loadAccounts">刷新数据</el-button>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>真实账号列表</span>
          </template>
          <el-table
            :data="authModel.accounts"
            border
            highlight-current-row
            row-key="username"
            @row-click="handleRowClick"
          >
            <el-table-column prop="username" label="账号" min-width="140" />
            <el-table-column prop="displayName" label="显示名" min-width="150" />
            <el-table-column label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="row.roleTagType">{{ row.roleLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="patientBinding" label="患者绑定" min-width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.statusType" effect="plain">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && authModel.accounts.length === 0" :description="authModel.emptyHint" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>角色概览</span>
          </template>
          <el-empty v-if="!loading && !authModel.activeRole" description="暂无角色数据" />
          <template v-else>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="当前角色">{{ authModel.activeRole?.label || '-' }}</el-descriptions-item>
              <el-descriptions-item label="账号数量">{{ authModel.activeRole?.count ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="权限范围">{{ authModel.activeRole?.scopeHint || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div class="summary-list">
              <div
                v-for="item in authModel.roleSummary"
                :key="item.role"
                class="summary-item"
                :class="{ active: item.role === authModel.activeRole?.role }"
                @click="selectRole(item.role)"
              >
                <div class="summary-header">
                  <el-tag :type="item.tagType" effect="plain">{{ item.label }}</el-tag>
                  <span class="summary-count">{{ item.count }} 个账号</span>
                </div>
                <div class="summary-hint">{{ item.scopeHint }}</div>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { buildAdminAuthModel, fetchAdminAuthAccounts } from "../../services/adminAuth.js";

const loading = ref(false);
const selectedRole = ref("");
const rawPayload = ref({ accounts: [], roleSummary: [] });
const authModel = ref(buildAdminAuthModel());

function applyPayload(payload) {
  rawPayload.value = payload;
  authModel.value = buildAdminAuthModel(payload, selectedRole.value);
  if (!selectedRole.value && authModel.value.activeRole?.role) {
    selectedRole.value = authModel.value.activeRole.role;
    authModel.value = buildAdminAuthModel(payload, selectedRole.value);
  }
}

function selectRole(role) {
  selectedRole.value = role;
  authModel.value = buildAdminAuthModel(rawPayload.value, selectedRole.value);
}

function handleRowClick(row) {
  selectRole(row.role);
}

async function loadAccounts() {
  loading.value = true;
  try {
    const payload = await fetchAdminAuthAccounts();
    applyPayload(payload);
  } catch (error) {
    rawPayload.value = { accounts: [], roleSummary: [] };
    authModel.value = buildAdminAuthModel();
    selectedRole.value = "";
    ElMessage.error(error.message || "账号与角色数据加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadAccounts();
});
</script>

<style scoped>
.admin-auth-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-card {
  border: 1px solid #fcd34d;
  background: linear-gradient(135deg, #fffbeb 0%, #ffffff 58%, #fef3c7 100%);
}

.hero-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #92400e;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #78716c;
  line-height: 1.6;
}

.summary-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-item {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fafaf9;
  cursor: pointer;
  transition: all 0.2s ease;
}

.summary-item.active {
  border-color: #f59e0b;
  background: #fff7ed;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.summary-count {
  color: #57534e;
  font-size: 12px;
}

.summary-hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .hero-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>