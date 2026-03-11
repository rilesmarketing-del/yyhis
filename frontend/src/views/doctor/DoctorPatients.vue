<template>
  <div class="doctor-patients-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>患者管理</span>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="搜索患者姓名或患者编号" clearable style="width: 260px" />
            <el-button @click="loadPatients">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredPatients" border v-loading="loading">
        <el-table-column prop="patientName" label="患者" width="120" />
        <el-table-column prop="patientId" label="患者编号" width="120" />
        <el-table-column prop="department" label="科室" width="120" />
        <el-table-column prop="latestVisitDate" label="最近就诊日期" width="140" />
        <el-table-column prop="latestDiagnosis" label="最近诊断" min-width="180" />
        <el-table-column prop="visitCount" label="接诊次数" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredPatients.length === 0" description="暂无患者记录" />
    </el-card>

    <el-drawer v-model="drawerVisible" title="患者详情" size="42%">
      <el-descriptions v-if="activePatient" :column="1" border>
        <el-descriptions-item label="患者">{{ activePatient.patientName }}</el-descriptions-item>
        <el-descriptions-item label="患者编号">{{ activePatient.patientId }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ activePatient.department }}</el-descriptions-item>
        <el-descriptions-item label="最近就诊日期">{{ activePatient.latestVisitDate }}</el-descriptions-item>
        <el-descriptions-item label="最近主诉">{{ activePatient.latestChiefComplaint || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="最近诊断">{{ activePatient.latestDiagnosis || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="累计接诊次数">{{ activePatient.visitCount }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { fetchDoctorPatients, sortDoctorPatients } from "../../services/doctorClinic";

const keyword = ref("");
const loading = ref(false);
const patients = ref([]);
const drawerVisible = ref(false);
const activePatient = ref(null);

const filteredPatients = computed(() => {
  const key = keyword.value.trim();
  if (!key) {
    return patients.value;
  }
  return patients.value.filter((item) => [item.patientName, item.patientId].some((value) => String(value || "").includes(key)));
});

async function loadPatients() {
  loading.value = true;
  try {
    patients.value = sortDoctorPatients(await fetchDoctorPatients());
  } catch (error) {
    ElMessage.error(error.message || "患者列表加载失败");
  } finally {
    loading.value = false;
  }
}

function openDetail(row) {
  activePatient.value = row;
  drawerVisible.value = true;
}

onMounted(() => {
  loadPatients();
});
</script>

<style scoped>
.doctor-patients-page {
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

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }
}
</style>