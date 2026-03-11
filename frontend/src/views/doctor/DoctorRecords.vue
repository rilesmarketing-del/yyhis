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
              <el-form-item label="处理建议">
                <el-input v-model="form.treatmentPlan" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="医嘱">
                <el-input v-model="form.doctorOrderNote" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="处方">
                <el-input v-model="form.prescriptionNote" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="报告说明">
                <el-input v-model="form.reportNote" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>

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
import { completeDoctorRecord, fetchDoctorRecord, fetchDoctorRecords, saveDoctorRecord, sortDoctorRecords, visitStatusMeta } from "../../services/doctorClinic";

const route = useRoute();
const loading = ref(false);
const saving = ref(false);
const completing = ref(false);
const records = ref([]);
const selectedRecord = ref(null);
const form = reactive({
  chiefComplaint: "",
  diagnosis: "",
  treatmentPlan: "",
  doctorOrderNote: "",
  prescriptionNote: "",
  reportNote: "",
});

function syncForm(record) {
  form.chiefComplaint = record?.chiefComplaint || "";
  form.diagnosis = record?.diagnosis || "";
  form.treatmentPlan = record?.treatmentPlan || "";
  form.doctorOrderNote = record?.doctorOrderNote || "";
  form.prescriptionNote = record?.prescriptionNote || "";
  form.reportNote = record?.reportNote || "";
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
    const saved = await saveDoctorRecord(selectedRecord.value.id, { ...form });
    selectedRecord.value = saved;
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
    const completed = await completeDoctorRecord(selectedRecord.value.id);
    selectedRecord.value = completed;
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

.record-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>