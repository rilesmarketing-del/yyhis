<template>
  <div class="admin-org-page">
    <el-row :gutter="12">
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">科室分布</div>
                <div class="panel-subtitle">维护真实科室结构，新增人员时可直接绑定到对应科室。</div>
              </div>
              <el-button type="primary" plain @click="openDepartmentDialog">新增科室</el-button>
            </div>
          </template>

          <el-tree
            v-if="orgModel.departmentTree.length > 0"
            :data="orgModel.departmentTree"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleDepartmentClick"
          />
          <el-empty v-else :description="orgModel.emptyHint" />
        </el-card>

        <el-card shadow="never" class="panel-card" v-loading="loading">
          <template #header>
            <span>角色统计</span>
          </template>
          <div v-if="orgModel.roleStats.length > 0" class="role-stats">
            <div v-for="item in orgModel.roleStats" :key="item.role" class="role-stat-item">
              <div class="role-stat-header">
                <el-tag :type="item.tagType" effect="plain">{{ item.label }}</el-tag>
                <strong>{{ item.count }}</strong>
              </div>
              <div class="role-stat-hint">当前系统内已创建的 {{ item.label }} 账号数量</div>
            </div>
          </div>
          <el-empty v-else description="暂无角色统计" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="17">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">人员管理</div>
                <div class="panel-subtitle">新增 doctor / admin / patient 账号后，可立即登录对应端口。</div>
              </div>
              <div class="header-actions">
                <el-button @click="loadSummary">刷新数据</el-button>
                <el-button type="primary" @click="openAccountDialog">新增人员</el-button>
              </div>
            </div>
          </template>

          <div class="filter-row">
            <el-input v-model="filters.keyword" placeholder="搜索用户名或显示名" clearable class="filter-input" />
            <el-select v-model="filters.role" placeholder="全部角色" clearable class="filter-select">
              <el-option v-for="item in adminOrgRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="filters.departmentId" placeholder="全部科室" clearable class="filter-select">
              <el-option v-for="item in orgModel.departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button text @click="resetFilters">清空筛选</el-button>
          </div>

          <el-table :data="filteredStaffs" border>
            <el-table-column prop="username" label="账号" min-width="140" />
            <el-table-column prop="displayName" label="显示名" min-width="130" />
            <el-table-column label="角色" width="110">
              <template #default="{ row }">
                <el-tag :type="row.roleTagType">{{ row.roleLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="departmentName" label="科室" min-width="140" />
            <el-table-column prop="title" label="职称" min-width="120" />
            <el-table-column prop="mobile" label="手机号" min-width="140" />
            <el-table-column prop="patientLabel" label="患者编号" min-width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.statusType" effect="plain">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && filteredStaffs.length === 0" description="暂无匹配人员" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="departmentDialogVisible" title="新增科室" width="460px">
      <el-form :model="departmentForm" label-width="92px">
        <el-form-item label="科室名称">
          <el-input v-model="departmentForm.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="上级科室">
          <el-select v-model="departmentForm.parentId" placeholder="顶级科室" clearable style="width: 100%">
            <el-option v-for="item in orgModel.departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="departmentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="departmentSubmitting" @click="submitDepartment">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="accountDialogVisible" title="新增人员" width="560px">
      <el-form :model="accountForm" label-width="96px">
        <el-form-item label="用户名">
          <el-input v-model="accountForm.username" placeholder="请输入唯一用户名" />
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="accountForm.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="accountForm.displayName" placeholder="请输入显示名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="accountForm.role" style="width: 100%">
            <el-option v-for="item in adminOrgRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属科室">
          <el-select v-model="accountForm.departmentId" placeholder="患者可不选" clearable style="width: 100%">
            <el-option v-for="item in orgModel.departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="accountForm.title" placeholder="医生或管理员可填写" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="accountForm.mobile" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="accountSubmitting" @click="submitAccount">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  adminOrgRoleOptions,
  buildAdminOrgModel,
  createAdminAccount,
  createAdminDepartment,
  fetchAdminOrgSummary,
} from "../../services/adminOrg.js";

const loading = ref(false);
const departmentSubmitting = ref(false);
const accountSubmitting = ref(false);
const summaryPayload = ref({ departments: [], staffs: [], roleStats: [] });
const orgModel = ref(buildAdminOrgModel());
const departmentDialogVisible = ref(false);
const accountDialogVisible = ref(false);

const filters = reactive({
  keyword: "",
  role: "",
  departmentId: null,
});

const departmentForm = reactive(createDepartmentForm());
const accountForm = reactive(createAccountForm());

function createDepartmentForm() {
  return {
    name: "",
    parentId: null,
  };
}

function createAccountForm() {
  return {
    username: "",
    password: "",
    displayName: "",
    role: "doctor",
    departmentId: null,
    title: "",
    mobile: "",
  };
}

function applySummary(summary) {
  summaryPayload.value = summary;
  orgModel.value = buildAdminOrgModel(summary);
}

function collectDescendantIds(nodes, targetId) {
  for (const item of nodes || []) {
    if (item.id === targetId) {
      return [item.id, ...flattenChildren(item.children || [])];
    }
    const nested = collectDescendantIds(item.children || [], targetId);
    if (nested.length > 0) {
      return nested;
    }
  }
  return [];
}

function flattenChildren(nodes) {
  return (nodes || []).flatMap((item) => [item.id, ...flattenChildren(item.children || [])]);
}

const matchedDepartmentIds = computed(() => {
  if (!filters.departmentId) {
    return [];
  }
  return collectDescendantIds(summaryPayload.value.departments || [], filters.departmentId);
});

const filteredStaffs = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase();
  return orgModel.value.staffs.filter((item) => {
    const matchKeyword = !keyword
      || [item.username, item.displayName].some((value) => String(value || "").toLowerCase().includes(keyword));
    const matchRole = !filters.role || item.role === filters.role;
    const matchDepartment = !filters.departmentId || matchedDepartmentIds.value.includes(item.departmentId);
    return matchKeyword && matchRole && matchDepartment;
  });
});

async function loadSummary() {
  loading.value = true;
  try {
    const summary = await fetchAdminOrgSummary();
    applySummary(summary);
  } catch (error) {
    applySummary({ departments: [], staffs: [], roleStats: [] });
    ElMessage.error(error.message || "组织与人员数据加载失败");
  } finally {
    loading.value = false;
  }
}

function openDepartmentDialog() {
  Object.assign(departmentForm, createDepartmentForm());
  departmentDialogVisible.value = true;
}

function openAccountDialog() {
  Object.assign(accountForm, createAccountForm());
  accountDialogVisible.value = true;
}

function handleDepartmentClick(node) {
  filters.departmentId = node.id;
}

function resetFilters() {
  filters.keyword = "";
  filters.role = "";
  filters.departmentId = null;
}

async function submitDepartment() {
  if (!departmentForm.name.trim()) {
    ElMessage.warning("请输入科室名称");
    return;
  }

  departmentSubmitting.value = true;
  try {
    await createAdminDepartment({
      name: departmentForm.name.trim(),
      parentId: departmentForm.parentId,
    });
    departmentDialogVisible.value = false;
    ElMessage.success("科室创建成功");
    await loadSummary();
  } catch (error) {
    ElMessage.error(error.message || "科室创建失败");
  } finally {
    departmentSubmitting.value = false;
  }
}

async function submitAccount() {
  if (!accountForm.username.trim() || !accountForm.password.trim() || !accountForm.displayName.trim()) {
    ElMessage.warning("请完整填写人员账号信息");
    return;
  }
  if (accountForm.role !== "patient" && !accountForm.departmentId) {
    ElMessage.warning("医生或管理员必须绑定科室");
    return;
  }

  accountSubmitting.value = true;
  try {
    await createAdminAccount({
      username: accountForm.username.trim(),
      password: accountForm.password,
      displayName: accountForm.displayName.trim(),
      role: accountForm.role,
      departmentId: accountForm.departmentId,
      title: accountForm.title.trim(),
      mobile: accountForm.mobile.trim(),
    });
    accountDialogVisible.value = false;
    ElMessage.success("人员账号创建成功");
    await loadSummary();
  } catch (error) {
    ElMessage.error(error.message || "人员账号创建失败");
  } finally {
    accountSubmitting.value = false;
  }
}

onMounted(() => {
  loadSummary();
});
</script>

<style scoped>
.admin-org-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-card {
  margin-bottom: 12px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.panel-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.filter-row {
  margin-bottom: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-input {
  width: 240px;
}

.filter-select {
  width: 180px;
}

.role-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.role-stat-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #fafaf9;
}

.role-stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-stat-hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .header-row,
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions,
  .filter-input,
  .filter-select {
    width: 100%;
  }
}
</style>