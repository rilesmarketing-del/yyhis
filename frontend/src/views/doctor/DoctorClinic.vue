<template>
  <el-row :gutter="12" class="doctor-clinic-page">
    <el-col :xs="24" :lg="14">
      <el-card shadow="never">
        <template #header>
          <div class="header-row">
            <span>待接诊队列</span>
            <div class="header-actions">
              <el-tag type="danger">当前等待 {{ queue.length }} 人</el-tag>
              <el-button @click="loadClinic">刷新</el-button>
            </div>
          </div>
        </template>

        <el-table :data="queue" border empty-text="暂无待接诊数据" v-loading="loading">
          <el-table-column prop="patientName" label="患者" width="120" />
          <el-table-column prop="department" label="科室" width="120" />
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="timeSlot" label="时段" width="130" />
          <el-table-column prop="paidAt" label="支付时间" min-width="160" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link :loading="startingAppointmentId === row.appointmentId" @click="handleStart(row)">
                开始接诊
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && queue.length === 0" description="当前没有待接诊患者" />
      </el-card>
    </el-col>

    <el-col :xs="24" :lg="10">
      <el-card shadow="never">
        <template #header>
          <span>当前接诊中</span>
        </template>

        <el-empty v-if="!currentVisit" description="暂无接诊中的患者" />
        <div v-else>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="患者">{{ currentVisit.patientName }}</el-descriptions-item>
            <el-descriptions-item label="科室 / 医生">{{ currentVisit.department }} / {{ currentVisit.doctorName }}</el-descriptions-item>
            <el-descriptions-item label="接诊时间">{{ currentVisit.visitDate }} {{ currentVisit.visitTimeSlot }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              {{ visitStatusMeta[currentVisit.status]?.label || currentVisit.status }}
            </el-descriptions-item>
            <el-descriptions-item label="诊断摘要">{{ currentVisit.diagnosis || '尚未填写' }}</el-descriptions-item>
          </el-descriptions>

          <div class="ops">
            <el-button type="primary" @click="goToRecord(currentVisit.id)">进入病历编辑</el-button>
            <el-button @click="goToPatients">查看患者列表</el-button>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchDoctorQueue, fetchDoctorRecords, pickCurrentVisit, sortDoctorQueue, startDoctorVisit, visitStatusMeta } from "../../services/doctorClinic";

const router = useRouter();
const loading = ref(false);
const queue = ref([]);
const currentVisit = ref(null);
const startingAppointmentId = ref("");

function goToRecord(visitId) {
  router.push({ path: "/doctor/records", query: { visitId } });
}

function goToPatients() {
  router.push("/doctor/patients");
}

async function loadClinic() {
  loading.value = true;
  try {
    const [queueData, records] = await Promise.all([fetchDoctorQueue(), fetchDoctorRecords()]);
    queue.value = sortDoctorQueue(queueData);
    currentVisit.value = pickCurrentVisit(records);
  } catch (error) {
    ElMessage.error(error.message || "医生接诊数据加载失败");
  } finally {
    loading.value = false;
  }
}

async function handleStart(row) {
  startingAppointmentId.value = row.appointmentId;
  try {
    const visit = await startDoctorVisit(row.appointmentId);
    currentVisit.value = visit;
    ElMessage.success(`已开始接诊：${row.patientName}`);
    await loadClinic();
    goToRecord(visit.id);
  } catch (error) {
    ElMessage.error(error.message || "开始接诊失败");
  } finally {
    startingAppointmentId.value = "";
  }
}

onMounted(() => {
  loadClinic();
});
</script>

<style scoped>
.doctor-clinic-page {
  display: flex;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ops {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
