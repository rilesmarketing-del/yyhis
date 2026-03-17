<template>
  <div class="prescriptions-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">处方与用药</div>
            <div class="panel-subtitle">展示医生完成接诊后开立的结构化处方条目，并兼容历史文字处方。</div>
          </div>
          <el-button @click="loadPrescriptions">刷新数据</el-button>
        </div>
      </template>

      <el-alert
        :title="`当前患者：${activePatient.name}`"
        type="success"
        :closable="false"
        show-icon
        class="patient-alert"
      />

      <div class="toolbar-row">
        <el-input
          v-model="keyword"
          placeholder="搜索处方编号 / 科室 / 医生 / 处方内容"
          clearable
          class="search-input"
        />
        <div class="toolbar-actions">
          <el-button @click="goTo('/patient/visits')">查看就诊记录</el-button>
          <el-button type="primary" plain @click="goTo('/patient/appointments')">去预约挂号</el-button>
        </div>
      </div>

      <el-skeleton v-if="loading" :rows="4" animated />

      <el-collapse v-else-if="filteredPrescriptions.length > 0" accordion>
        <el-collapse-item
          v-for="item in filteredPrescriptions"
          :key="item.presNo"
          :name="item.presNo"
        >
          <template #title>
            <div class="collapse-title">
              <div class="collapse-meta">
                <span class="pres-no">{{ item.presNo }}</span>
                <span>{{ item.dept }}</span>
                <span>{{ item.date }}</span>
                <span>{{ item.doctorName }}</span>
              </div>
              <el-tag size="small" type="success">{{ item.status }}</el-tag>
            </div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="处方编号">{{ item.presNo }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ item.dept }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ item.doctorName }}</el-descriptions-item>
            <el-descriptions-item label="开立时间">{{ item.date }}</el-descriptions-item>
            <el-descriptions-item label="处方摘要">
              <div class="prescription-content">{{ item.content }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="item.items.length > 0" label="条目明细">
              <div class="prescription-items">
                <div v-for="entry in item.items" :key="entry.id || entry.drugName" class="prescription-item">
                  <div class="drug-name">{{ entry.drugName }}</div>
                  <div class="drug-meta">{{ [entry.dosage, entry.frequency, entry.days ? `${entry.days}天` : '', entry.remark].filter(Boolean).join('，') }}</div>
                </div>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
      </el-collapse>

      <el-empty v-else description="暂无可查看处方">
        <el-button @click="goTo('/patient/visits')">查看就诊记录</el-button>
        <el-button type="primary" plain @click="goTo('/patient/appointments')">去预约挂号</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchPatientVisits } from "../../services/doctorClinic.js";
import { buildPatientPrescriptions } from "../../services/patientPrescriptions.js";
import { getActivePatient } from "../../services/patientSession";

const router = useRouter();
const keyword = ref("");
const loading = ref(false);
const prescriptions = ref([]);
const activePatient = ref(getActivePatient());

const filteredPrescriptions = computed(() => {
  const key = keyword.value.trim();
  if (!key) {
    return prescriptions.value;
  }
  return prescriptions.value.filter((item) =>
    [item.presNo, item.dept, item.doctorName, item.content, ...(item.items || []).map((entry) => entry.drugName)].some((value) => String(value || "").includes(key))
  );
});

function goTo(path) {
  router.push(path);
}

async function loadPrescriptions() {
  loading.value = true;
  activePatient.value = getActivePatient();
  try {
    const visitRecords = await fetchPatientVisits();
    prescriptions.value = buildPatientPrescriptions({ visitRecords });
  } catch (error) {
    prescriptions.value = [];
    ElMessage.error(error.message || "处方加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadPrescriptions();
});
</script>

<style scoped>
.prescriptions-page {
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

.patient-alert {
  margin-bottom: 16px;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  max-width: 420px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.collapse-title {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-right: 12px;
}

.collapse-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: #475569;
}

.pres-no {
  font-weight: 600;
  color: #0f766e;
}

.prescription-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}

.prescription-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.prescription-item {
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.drug-name {
  font-weight: 600;
  color: #0f172a;
}

.drug-meta {
  margin-top: 4px;
  color: #475569;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .header-row,
  .toolbar-row,
  .collapse-title {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-input,
  .toolbar-actions {
    width: 100%;
  }

  .toolbar-actions {
    justify-content: space-between;
  }
}
</style>
