<template>
  <div class="doctor-records-page">
    <el-row :gutter="12">
      <el-col :xs="24" :lg="11">
        <el-card shadow="never">
          <template #header>
            <div class="header-row">
              <span>接诊记录</span>
              <el-button @click="loadRecords">刷新</el-button>
            </div>
          </template>

          <el-table :data="records" border v-loading="loading" @row-click="handleSelect">
            <el-table-column prop="patientName" label="患者" width="110" />
            <el-table-column prop="department" label="科室" width="110" />
            <el-table-column label="就诊时间" min-width="150">
              <template #default="{ row }">{{ row.visitDate }} {{ row.visitTimeSlot }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="visitStatusMeta[row.status]?.type || 'info'">
                  {{ visitStatusMeta[row.status]?.label || row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loading && records.length === 0" description="暂无接诊记录" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="13">
        <el-card shadow="never">
          <template #header>
            <div class="header-row">
              <span>病历编辑</span>
              <el-tag v-if="selectedRecord" :type="visitStatusMeta[selectedRecord.status]?.type || 'info'">
                {{ visitStatusMeta[selectedRecord.status]?.label || selectedRecord.status }}
              </el-tag>
            </div>
          </template>

          <el-empty v-if="!selectedRecord" description="请选择一条接诊记录" />

          <div v-else>
            <el-descriptions :column="1" border class="record-summary">
              <el-descriptions-item label="患者">{{ selectedRecord.patientName }}</el-descriptions-item>
              <el-descriptions-item label="科室 / 医生">{{ selectedRecord.department }} / {{ selectedRecord.doctorName }}</el-descriptions-item>
              <el-descriptions-item label="就诊时间">{{ selectedRecord.visitDate }} {{ selectedRecord.visitTimeSlot }}</el-descriptions-item>
            </el-descriptions>

            <el-form label-position="top" class="record-form">
              <el-form-item label="主诉">
                <el-input v-model="form.chiefComplaint" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="诊断">
                <el-input v-model="form.diagnosis" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="治疗计划">
                <el-input v-model="form.treatmentPlan" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>

            <div class="structured-sections">
              <el-card shadow="never" class="structured-card">
                <template #header>
                  <div class="section-header">
                    <span>医嘱条目</span>
                    <el-button type="primary" link @click="addDoctorOrder">新增医嘱</el-button>
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

              <el-card shadow="never" class="structured-card">
                <template #header>
                  <div class="section-header">
                    <span>处方条目</span>
                    <el-button type="primary" link @click="addPrescription">新增处方</el-button>
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

              <el-card shadow="never" class="structured-card">
                <template #header>
                  <div class="section-header">
                    <span>报告条目</span>
                    <el-button type="primary" link @click="addReport">新增报告</el-button>
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
            </div>

            <div class="record-actions">
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
              <el-button
                type="success"
                :loading="completing"
                :disabled="selectedRecord.status === 'COMPLETED'"
                @click="handleComplete"
              >
                结束接诊
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  buildVisitRecordForm,
  buildVisitRecordPayload,
  completeDoctorRecord,
  createDoctorOrderItem,
  createPrescriptionItem,
  createReportItem,
  doctorOrderCategoryOptions,
  doctorOrderPriorityOptions,
  fetchDoctorRecord,
  fetchDoctorRecords,
  reportFlagOptions,
  saveDoctorRecord,
  sortDoctorRecords,
  visitStatusMeta,
} from "../../services/doctorClinic.js";

const route = useRoute();
const loading = ref(false);
const saving = ref(false);
const completing = ref(false);
const records = ref([]);
const selectedRecord = ref(null);
const form = reactive(buildVisitRecordForm(null));

function replaceList(target, items) {
  target.splice(0, target.length, ...items);
}

function syncForm(record) {
  const nextForm = buildVisitRecordForm(record);
  form.chiefComplaint = nextForm.chiefComplaint;
  form.diagnosis = nextForm.diagnosis;
  form.treatmentPlan = nextForm.treatmentPlan;
  replaceList(form.doctorOrders, nextForm.doctorOrders);
  replaceList(form.prescriptions, nextForm.prescriptions);
  replaceList(form.reports, nextForm.reports);
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

async function selectRecord(visitId) {
  if (!visitId) {
    selectedRecord.value = null;
    syncForm(null);
    return;
  }
  try {
    const record = await fetchDoctorRecord(visitId);
    selectedRecord.value = record;
    syncForm(record);
  } catch (error) {
    ElMessage.error(error.message || "接诊记录加载失败");
  }
}

async function loadRecords() {
  loading.value = true;
  try {
    const result = await fetchDoctorRecords();
    records.value = sortDoctorRecords(result);
    const routeVisitId = typeof route.query.visitId === "string" ? route.query.visitId : "";
    const targetId = routeVisitId || records.value[0]?.id || "";
    await selectRecord(targetId);
  } catch (error) {
    ElMessage.error(error.message || "接诊记录加载失败");
  } finally {
    loading.value = false;
  }
}

function handleSelect(row) {
  selectRecord(row.id);
}

async function handleSave() {
  if (!selectedRecord.value) {
    return;
  }
  saving.value = true;
  try {
    const saved = await saveDoctorRecord(selectedRecord.value.id, buildVisitRecordPayload(form));
    selectedRecord.value = saved;
    syncForm(saved);
    ElMessage.success("病历已保存");
    await loadRecords();
  } catch (error) {
    ElMessage.error(error.message || "病历保存失败");
  } finally {
    saving.value = false;
  }
}

async function handleComplete() {
  if (!selectedRecord.value) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认结束 ${selectedRecord.value.patientName} 的接诊吗？`, "结束接诊", {
      type: "warning",
    });
    completing.value = true;
    const saved = await saveDoctorRecord(selectedRecord.value.id, buildVisitRecordPayload(form));
    selectedRecord.value = saved;
    syncForm(saved);
    const completed = await completeDoctorRecord(selectedRecord.value.id);
    selectedRecord.value = completed;
    syncForm(completed);
    ElMessage.success("接诊已完成");
    await loadRecords();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error.message || "结束接诊失败");
    }
  } finally {
    completing.value = false;
  }
}

watch(
  () => route.query.visitId,
  (visitId) => {
    if (typeof visitId === "string" && visitId) {
      selectRecord(visitId);
    }
  }
);

onMounted(() => {
  loadRecords();
});
</script>

<style scoped>
.doctor-records-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.record-summary {
  margin-bottom: 16px;
}

.record-form {
  margin-top: 12px;
}

.structured-sections {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.structured-card {
  border-color: #dbeafe;
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

.record-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .header-row,
  .record-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-grid.two-col {
    grid-template-columns: 1fr;
  }
}
</style>
