<template>
  <div class="doctor-orders-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">当前接诊工作台</div>
            <div class="panel-subtitle">快速维护当前接诊患者的结构化医嘱、处方和报告条目。</div>
          </div>
          <div class="header-actions">
            <el-button @click="loadWorkbench">刷新</el-button>
            <el-button type="primary" plain @click="goTo('/doctor/records')">前往完整病历</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="6" animated />

      <el-empty v-else-if="!currentVisit" description="当前没有接诊中的患者">
        <el-button type="primary" @click="goTo('/doctor/clinic')">前往接诊台</el-button>
      </el-empty>

      <div v-else class="workbench-body">
        <el-descriptions :column="2" border class="summary-card">
          <el-descriptions-item label="患者">{{ currentVisit.patientName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="visitStatusMeta[currentVisit.status]?.type || 'info'">
              {{ visitStatusMeta[currentVisit.status]?.label || currentVisit.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="科室 / 医生">{{ currentVisit.department }} / {{ currentVisit.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="就诊时间">{{ currentVisit.visitDate }} {{ currentVisit.visitTimeSlot }}</el-descriptions-item>
        </el-descriptions>

        <el-row :gutter="12">
          <el-col :xs="24" :lg="8">
            <el-card shadow="never" class="editor-card">
              <template #header>
                <div class="section-header">
                  <span>医嘱</span>
                  <el-button type="primary" link @click="addDoctorOrder">新增</el-button>
                </div>
              </template>

              <el-empty v-if="form.doctorOrders.length === 0" description="暂无医嘱条目" />

              <div v-else class="editor-list">
                <div v-for="(item, index) in form.doctorOrders" :key="item.id" class="editor-item">
                  <div class="item-header">
                    <span>医嘱 {{ index + 1 }}</span>
                    <el-button link type="danger" @click="removeDoctorOrder(index)">删除</el-button>
                  </div>
                  <div class="item-grid two-col">
                    <el-select v-model="item.category" placeholder="选择类别">
                      <el-option v-for="option in doctorOrderCategoryOptions" :key="option.value" :label="option.label" :value="option.value" />
                    </el-select>
                    <el-select v-model="item.priority" placeholder="选择优先级">
                      <el-option v-for="option in doctorOrderPriorityOptions" :key="option.value" :label="option.label" :value="option.value" />
                    </el-select>
                  </div>
                  <el-input v-model="item.content" type="textarea" :rows="3" placeholder="填写具体医嘱内容" />
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :lg="8">
            <el-card shadow="never" class="editor-card">
              <template #header>
                <div class="section-header">
                  <span>处方</span>
                  <el-button type="primary" link @click="addPrescription">新增</el-button>
                </div>
              </template>

              <el-empty v-if="form.prescriptions.length === 0" description="暂无处方条目" />

              <div v-else class="editor-list">
                <div v-for="(item, index) in form.prescriptions" :key="item.id" class="editor-item">
                  <div class="item-header">
                    <span>处方 {{ index + 1 }}</span>
                    <el-button link type="danger" @click="removePrescription(index)">删除</el-button>
                  </div>
                  <div class="item-grid two-col">
                    <el-input v-model="item.drugName" placeholder="药品名" />
                    <el-input v-model="item.dosage" placeholder="剂量" />
                    <el-input v-model="item.frequency" placeholder="频次" />
                    <el-input v-model="item.days" placeholder="天数" />
                  </div>
                  <el-input v-model="item.remark" placeholder="备注" />
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :lg="8">
            <el-card shadow="never" class="editor-card">
              <template #header>
                <div class="section-header">
                  <span>报告</span>
                  <el-button type="primary" link @click="addReport">新增</el-button>
                </div>
              </template>

              <el-empty v-if="form.reports.length === 0" description="暂无报告条目" />

              <div v-else class="editor-list">
                <div v-for="(item, index) in form.reports" :key="item.id" class="editor-item">
                  <div class="item-header">
                    <span>报告 {{ index + 1 }}</span>
                    <el-button link type="danger" @click="removeReport(index)">删除</el-button>
                  </div>
                  <div class="item-grid two-col">
                    <el-input v-model="item.itemName" placeholder="项目名" />
                    <el-select v-model="item.resultFlag" placeholder="结果标记">
                      <el-option v-for="option in reportFlagOptions" :key="option.value" :label="option.label" :value="option.value" />
                    </el-select>
                  </div>
                  <el-input v-model="item.resultSummary" type="textarea" :rows="3" placeholder="填写结果摘要" />
                  <el-input v-model="item.advice" type="textarea" :rows="2" placeholder="填写随访建议" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="workbench-actions">
          <el-button type="primary" :loading="saving" @click="handleSave">保存当前工作台</el-button>
          <el-button @click="goTo(`/doctor/records?visitId=${currentVisit.id}`)">前往完整病历</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  buildDoctorOrdersDraft,
  buildStructuredCollectionsPayload,
  createDoctorOrderItem,
  createPrescriptionItem,
  createReportItem,
  doctorOrderCategoryOptions,
  doctorOrderPriorityOptions,
  fetchDoctorRecord,
  fetchDoctorRecords,
  pickCurrentVisit,
  reportFlagOptions,
  saveDoctorRecord,
  visitStatusMeta,
} from "../../services/doctorClinic.js";

const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const currentVisit = ref(null);
const form = reactive(buildDoctorOrdersDraft(null));

function replaceList(target, items) {
  target.splice(0, target.length, ...items);
}

function syncDraft(record) {
  const draft = buildDoctorOrdersDraft(record);
  replaceList(form.doctorOrders, draft.doctorOrders);
  replaceList(form.prescriptions, draft.prescriptions);
  replaceList(form.reports, draft.reports);
}

function addDoctorOrder() {
  form.doctorOrders.push(createDoctorOrderItem());
}

function removeDoctorOrder(index) {
  form.doctorOrders.splice(index, 1);
}

function addPrescription() {
  form.prescriptions.push(createPrescriptionItem());
}

function removePrescription(index) {
  form.prescriptions.splice(index, 1);
}

function addReport() {
  form.reports.push(createReportItem());
}

function removeReport(index) {
  form.reports.splice(index, 1);
}

function goTo(path) {
  router.push(path);
}

async function loadWorkbench() {
  loading.value = true;
  try {
    const records = await fetchDoctorRecords();
    const current = pickCurrentVisit(records);
    if (!current) {
      currentVisit.value = null;
      syncDraft(null);
      return;
    }
    const detail = await fetchDoctorRecord(current.id);
    currentVisit.value = detail;
    syncDraft(detail);
  } catch (error) {
    currentVisit.value = null;
    syncDraft(null);
    ElMessage.error(error.message || "工作台加载失败");
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  if (!currentVisit.value) {
    return;
  }
  saving.value = true;
  try {
    await saveDoctorRecord(currentVisit.value.id, buildVisitRecordPayload({ ...currentVisit.value, ...form }));
    const detail = await fetchDoctorRecord(currentVisit.value.id);
    currentVisit.value = detail;
    syncDraft(detail);
    ElMessage.success("工作台内容已保存");
  } catch (error) {
    ElMessage.error(error.message || "工作台保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  loadWorkbench();
});
</script>

<style scoped>
.doctor-orders-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  color: #164e63;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.workbench-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card {
  margin-bottom: 4px;
}

.editor-card {
  height: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.editor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-item {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: #f8fbff;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #0f172a;
}

.item-grid {
  display: grid;
  gap: 10px;
}

.item-grid.two-col {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.workbench-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 900px) {
  .header-row,
  .header-actions,
  .workbench-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .item-grid.two-col {
    grid-template-columns: 1fr;
  }
}
</style>