<template>
  <div class="admin-org-page">
    <el-row :gutter="12">
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="panel-card" v-loading="loading">
          <template #header>
            <div class="header-row">
              <div>
                <div class="panel-title">科室分布</div>
                <div class="panel-subtitle">维护真实科室结构，支持编辑名称和调整上级关系。</div>
              </div>
              <el-button type="primary" plain @click="openDepartmentCreateDialog">新增科室</el-button>
            </div>
          </template>

          <el-tree
            v-if="orgModel.departmentTree.length > 0"
            :data="orgModel.departmentTree"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleDepartmentClick"
          >
            <template #default="{ data }">
              <div class="tree-node">
                <span class="tree-node-label">{{ data.label }}</span>
                <el-button text type="primary" @click.stop="openDepartmentEditDialog(data)">编辑科室</el-button>
              </div>
            </template>
          </el-tree>
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
                <div class="panel-subtitle">支持编辑账号资料、启停账号和重置默认密码。</div>
              </div>
              <div class="header-actions">
                <el-button @click="loadSummary">刷新数据</el-button>
                <el-button type="primary" @click="openAccountCreateDialog">新增人员</el-button>
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

          <el-alert
            type="info"
            show-icon
            :closable="false"
            class="org-tip"
            title="密码重置后将统一恢复为 123456，系统已禁止当前登录管理员停用自己。"
          />

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
            <el-table-column label="操作" min-width="240" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="openAccountEditDialog(row)">编辑账号</el-button>
                  <el-button
                    link
                    :type="row.enabled ? 'danger' : 'success'"
                    :disabled="isSelfAccount(row)"
                    @click="handleToggleAccount(row)"
                  >
                    {{ row.enabled ? '停用账号' : '启用账号' }}
                  </el-button>
                  <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && filteredStaffs.length === 0" description="暂无匹配人员" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="departmentDialogVisible" :title="departmentDialogTitle" width="460px">
      <el-form :model="departmentForm" label-width="92px">
        <el-form-item label="科室名称">
          <el-input v-model="departmentForm.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="上级科室">
          <el-select v-model="departmentForm.parentId" placeholder="顶级科室" clearable style="width: 100%">
            <el-option v-for="item in availableParentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="departmentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="departmentSubmitting" @click="submitDepartment">{{ departmentSubmitText }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="accountDialogVisible" :title="accountDialogTitle" width="560px">
      <el-form :model="accountForm" label-width="96px">
        <el-form-item label="用户名">
          <el-input v-model="accountForm.username" :disabled="isAccountEditMode" placeholder="请输入唯一用户名" />
        </el-form-item>
        <el-form-item v-if="!isAccountEditMode" label="初始密码">
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
          <el-select
            v-model="accountForm.departmentId"
            placeholder="患者可不绑定科室"
            clearable
            style="width: 100%"
            :disabled="accountForm.role === 'patient'"
          >
            <el-option v-for="item in orgModel.departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="accountForm.title" :disabled="accountForm.role === 'patient'" placeholder="医生或管理员可填写" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="accountForm.mobile" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="accountSubmitting" @click="submitAccount">{{ accountSubmitText }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCurrentUser } from "../../services/auth.js";
import {
  adminOrgRoleOptions,
  buildAdminOrgModel,
  createAdminAccount,
  createAdminDepartment,
  disableAdminAccount,
  enableAdminAccount,
  fetchAdminOrgSummary,
  resetAdminAccountPassword,
  updateAdminAccount,
  updateAdminDepartment,
} from "../../services/adminOrg.js";

const loading = ref(false);
const departmentSubmitting = ref(false);
const accountSubmitting = ref(false);
const summaryPayload = ref({ departments: [], staffs: [], roleStats: [] });
const orgModel = ref(buildAdminOrgModel());
const departmentDialogVisible = ref(false);
const accountDialogVisible = ref(false);
const departmentDialogMode = ref("create");
const accountDialogMode = ref("create");

const filters = reactive({
  keyword: "",
  role: "",
  departmentId: null,
});

const departmentForm = reactive(createDepartmentForm());
const accountForm = reactive(createAccountForm());

const currentAdminUsername = computed(() => getCurrentUser()?.username || "");
const isAccountEditMode = computed(() => accountDialogMode.value === "edit");
const departmentDialogTitle = computed(() => (departmentDialogMode.value === "edit" ? "编辑科室" : "新增科室"));
const accountDialogTitle = computed(() => (accountDialogMode.value === "edit" ? "编辑账号" : "新增人员"));
const departmentSubmitText = computed(() => (departmentDialogMode.value === "edit" ? "保存修改" : "创建科室"));
const accountSubmitText = computed(() => (accountDialogMode.value === "edit" ? "保存修改" : "创建账号"));

function createDepartmentForm() {
  return {
    id: null,
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
  if (filters.departmentId && !orgModel.value.departmentOptions.some((item) => item.value === filters.departmentId)) {
    filters.departmentId = null;
  }
}

function flattenChildren(nodes) {
  return (nodes || []).flatMap((item) => [item.id, ...flattenChildren(item.children || [])]);
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

const matchedDepartmentIds = computed(() => {
  if (!filters.departmentId) {
    return [];
  }
  return collectDescendantIds(summaryPayload.value.departments || [], filters.departmentId);
});

const availableParentOptions = computed(() => {
  if (departmentDialogMode.value !== "edit" || !departmentForm.id) {
    return orgModel.value.departmentOptions;
  }
  const blockedIds = new Set(collectDescendantIds(summaryPayload.value.departments || [], departmentForm.id));
  return orgModel.value.departmentOptions.filter((item) => !blockedIds.has(item.value));
});

const filteredStaffs = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase();
  return orgModel.value.staffs.filter((item) => {
    const matchKeyword = !keyword || [item.username, item.displayName].some((value) => String(value || "").toLowerCase().includes(keyword));
    const matchRole = !filters.role || item.role === filters.role;
    const matchDepartment = !filters.departmentId || matchedDepartmentIds.value.includes(item.departmentId);
    return matchKeyword && matchRole && matchDepartment;
  });
});

watch(
  () => accountForm.role,
  (role) => {
    if (role === "patient") {
      accountForm.departmentId = null;
      accountForm.title = "";
    }
  }
);

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

function openDepartmentCreateDialog() {
  departmentDialogMode.value = "create";
  Object.assign(departmentForm, createDepartmentForm());
  departmentDialogVisible.value = true;
}

function openDepartmentEditDialog(node) {
  departmentDialogMode.value = "edit";
  Object.assign(departmentForm, {
    id: node.id,
    name: node.name,
    parentId: node.parentId,
  });
  departmentDialogVisible.value = true;
}

function openAccountCreateDialog() {
  accountDialogMode.value = "create";
  Object.assign(accountForm, createAccountForm());
  accountDialogVisible.value = true;
}

function openAccountEditDialog(row) {
  accountDialogMode.value = "edit";
  Object.assign(accountForm, {
    username: row.username,
    password: "",
    displayName: row.displayName,
    role: row.role,
    departmentId: row.role === "patient" ? null : row.departmentId,
    title: row.title === "-" ? "" : row.title,
    mobile: row.mobile === "-" ? "" : row.mobile,
  });
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

function isSelfAccount(row) {
  return row.username === currentAdminUsername.value;
}

async function submitDepartment() {
  const name = departmentForm.name.trim();
  if (!name) {
    ElMessage.warning("请输入科室名称");
    return;
  }

  departmentSubmitting.value = true;
  try {
    if (departmentDialogMode.value === "edit") {
      await updateAdminDepartment(departmentForm.id, {
        name,
        parentId: departmentForm.parentId,
      });
      ElMessage.success("科室信息已更新");
    } else {
      await createAdminDepartment({
        name,
        parentId: departmentForm.parentId,
      });
      ElMessage.success("科室创建成功");
    }
    departmentDialogVisible.value = false;
    await loadSummary();
  } catch (error) {
    ElMessage.error(error.message || (departmentDialogMode.value === "edit" ? "科室更新失败" : "科室创建失败"));
  } finally {
    departmentSubmitting.value = false;
  }
}

async function submitAccount() {
  const username = accountForm.username.trim();
  const displayName = accountForm.displayName.trim();
  const title = accountForm.title.trim();
  const mobile = accountForm.mobile.trim();

  if (!displayName) {
    ElMessage.warning("请输入显示名");
    return;
  }
  if (!isAccountEditMode.value && (!username || !accountForm.password.trim())) {
    ElMessage.warning("请完整填写新增账号信息");
    return;
  }
  if (accountForm.role !== "patient" && !accountForm.departmentId) {
    ElMessage.warning("医生或管理员必须绑定科室");
    return;
  }

  const payload = {
    displayName,
    role: accountForm.role,
    departmentId: accountForm.role === "patient" ? null : accountForm.departmentId,
    title: accountForm.role === "patient" ? "" : title,
    mobile,
  };

  accountSubmitting.value = true;
  try {
    if (isAccountEditMode.value) {
      await updateAdminAccount(accountForm.username, payload);
      ElMessage.success("账号资料已更新");
    } else {
      await createAdminAccount({
        username,
        password: accountForm.password.trim(),
        ...payload,
      });
      ElMessage.success("人员账号创建成功");
    }
    accountDialogVisible.value = false;
    await loadSummary();
  } catch (error) {
    ElMessage.error(error.message || (isAccountEditMode.value ? "账号更新失败" : "账号创建失败"));
  } finally {
    accountSubmitting.value = false;
  }
}

async function handleToggleAccount(row) {
  if (isSelfAccount(row)) {
    ElMessage.warning("不能停用当前登录管理员");
    return;
  }

  const enabling = !row.enabled;
  try {
    await ElMessageBox.confirm(
      enabling
        ? `确认重新启用账号 ${row.username} 吗？`
        : `确认停用账号 ${row.username} 吗？停用后该账号将无法登录。`,
      enabling ? "启用账号" : "停用账号",
      { type: enabling ? "info" : "warning" }
    );
  } catch (error) {
    return;
  }

  try {
    if (enabling) {
      await enableAdminAccount(row.username);
      ElMessage.success(`账号 ${row.username} 已启用`);
    } else {
      await disableAdminAccount(row.username);
      ElMessage.success(`账号 ${row.username} 已停用`);
    }
    await loadSummary();
  } catch (error) {
    ElMessage.error(error.message || (enabling ? "账号启用失败" : "账号停用失败"));
  }
}

async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确认将账号 ${row.username} 的密码重置为默认密码 123456 吗？`,
      "重置密码",
      { type: "warning" }
    );
  } catch (error) {
    return;
  }

  try {
    await resetAdminAccountPassword(row.username);
    ElMessage.success(`账号 ${row.username} 已重置为默认密码 123456`);
    await loadSummary();
  } catch (error) {
    ElMessage.error(error.message || "密码重置失败");
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
  line-height: 1.6;
}

.tree-node {
  width: 100%;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tree-node-label {
  min-width: 0;
  color: #111827;
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

.org-tip {
  margin-bottom: 12px;
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

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

  .tree-node {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>