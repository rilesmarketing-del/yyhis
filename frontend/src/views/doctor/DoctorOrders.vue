<template>
  <div class="doctor-orders-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">当前接诊工作台</div>
            <div class="panel-subtitle">快速维护当前接诊患者的医嘱、处方和报告内容。</div>
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
                <span>医嘱</span>
              </template>
              <el-input v-model="form.doctorOrderNote" type="textarea" :rows="10" placeholder="填写医生医嘱" />
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="8">
            <el-card shadow="never" class="editor-card">
              <template #header>
                <span>处方</span>
              </template>
              <el-input v-model="form.prescriptionNote" type="textarea" :rows="10" placeholder="填写文字处方内容" />
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="8">
            <el-card shadow="never" class="editor-card">
              <template #header>
                <span>报告</span>
              </template>
              <el-input v-model="form.reportNote" type="textarea" :rows="10" placeholder="填写报告说明或随访结论" />
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
  fetchDoctorRecord,
  fetchDoctorRecords,
  pickCurrentVisit,
  saveDoctorRecord,
  visitStatusMeta,
} from "../../services/doctorClinic.js";

const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const currentVisit = ref(null);
const form = reactive(buildDoctorOrdersDraft(null));

function syncDraft(record) {
  const draft = buildDoctorOrdersDraft(record);
  form.doctorOrderNote = draft.doctorOrderNote;
  form.prescriptionNote = draft.prescriptionNote;
  form.reportNote = draft.reportNote;
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
    await saveDoctorRecord(currentVisit.value.id, {
      doctorOrderNote: form.doctorOrderNote,
      prescriptionNote: form.prescriptionNote,
      reportNote: form.reportNote,
    });
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
}
</style>
