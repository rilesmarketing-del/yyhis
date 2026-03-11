<template>
  <div class="visits-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">就诊记录</div>
            <div class="panel-subtitle">优先展示医生真实接诊记录，没有时再回退到预约映射记录</div>
          </div>
          <el-button @click="loadVisits">刷新数据</el-button>
        </div>
      </template>

      <el-alert
        :title="`当前登录患者：${activePatient.name}（${activePatient.id}）`"
        type="success"
        :closable="false"
        show-icon
        class="patient-alert"
      />

      <div class="toolbar-row">
        <el-input
          v-model="keyword"
          placeholder="搜索就诊号 / 科室 / 医生 / 关联预约"
          clearable
          class="search-input"
        />
        <div class="toolbar-actions">
          <el-button @click="goTo('/patient/appointments')">去预约挂号</el-button>
          <el-button type="warning" plain @click="goTo('/patient/payments')">去支付</el-button>
        </div>
      </div>

      <el-table :data="filteredVisits" border v-loading="loading">
        <el-table-column prop="visitNo" label="就诊号" width="130" />
        <el-table-column prop="dept" label="科室" width="120" />
        <el-table-column prop="doctor" label="医生" width="120" />
        <el-table-column prop="source" label="记录来源" width="110" />
        <el-table-column prop="date" label="就诊日期" min-width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredVisits.length === 0" description="暂无就诊记录">
        <el-button type="primary" @click="goTo('/patient/appointments')">去预约挂号</el-button>
        <el-button type="warning" plain @click="goTo('/patient/payments')">去支付待支付账单</el-button>
      </el-empty>
    </el-card>

    <el-drawer v-model="drawerVisible" title="就诊详情" size="45%">
      <el-descriptions v-if="currentVisit" :column="1" border>
        <el-descriptions-item label="就诊号">{{ currentVisit.visitNo }}</el-descriptions-item>
        <el-descriptions-item label="科室 / 医生">{{ currentVisit.dept }} / {{ currentVisit.doctor }}</el-descriptions-item>
        <el-descriptions-item label="关联预约">{{ currentVisit.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="就诊时间">{{ currentVisit.date }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentVisit.paidAt }}</el-descriptions-item>
        <el-descriptions-item label="诊断">{{ currentVisit.diagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理建议">{{ currentVisit.treatmentPlan || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医嘱">{{ currentVisit.doctorOrderNote || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处方">{{ currentVisit.prescriptionNote || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告说明">{{ currentVisit.reportNote || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下一步建议">{{ currentVisit.nextStep }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchMyAppointments } from "../../services/patientAppointments";
import { fetchPatientVisits } from "../../services/doctorClinic";
import { getActivePatient } from "../../services/patientSession";
import { buildPatientVisits } from "../../services/patientVisits";

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

const router = useRouter();
const keyword = ref("");
const loading = ref(false);
const drawerVisible = ref(false);
const currentVisit = ref(null);
const activePatient = ref(getActivePatient());
const visits = ref([]);

const filteredVisits = computed(() => {
  const key = keyword.value.trim();
  if (!key) {
    return visits.value;
  }

  return visits.value.filter((item) => {
    return [item.visitNo, item.dept, item.doctor, item.serialNumber].some((value) => String(value || "").includes(key));
  });
});

function goTo(path) {
  router.push(path);
}

function openDetail(visit) {
  currentVisit.value = visit;
  drawerVisible.value = true;
}

async function loadVisits() {
  loading.value = true;
  activePatient.value = getActivePatient();
  try {
    const [visitRecords, appointments] = await Promise.all([fetchPatientVisits(), fetchMyAppointments()]);
    visits.value = buildPatientVisits({ appointments, visitRecords, today: getToday() });
  } catch (error) {
    visits.value = [];
    ElMessage.error(error.message || "就诊记录加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadVisits();
});
</script>

<style scoped>
.visits-page {
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
  max-width: 360px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 900px) {
  .header-row,
  .toolbar-row {
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