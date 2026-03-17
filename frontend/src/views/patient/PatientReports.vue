<template>
  <div class="reports-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">检查报告</div>
            <div class="panel-subtitle">优先展示医生完成接诊后生成的真实结构化报告，缺失时回退预约映射报告。</div>
          </div>
          <el-button @click="loadReports">刷新数据</el-button>
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
          placeholder="搜索报告编号 / 项目 / 科室 / 医生 / 关联预约单号"
          clearable
          class="search-input"
        />
        <div class="toolbar-actions">
          <el-button @click="goTo('/patient/visits')">查看就诊记录</el-button>
          <el-button type="primary" plain @click="goTo('/patient/appointments')">去预约挂号</el-button>
        </div>
      </div>

      <el-table :data="filteredReports" border v-loading="loading">
        <el-table-column prop="reportNo" label="报告编号" width="140" />
        <el-table-column prop="item" label="项目" min-width="180" />
        <el-table-column prop="date" label="出具时间" width="180" />
        <el-table-column label="结果" width="120">
          <template #default="{ row }">
            <el-tag :type="reportTagType(row)">{{ row.resultLabel || row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="结论摘要" min-width="260" />
        <el-table-column prop="source" label="来源" width="110" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="preview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && filteredReports.length === 0" description="暂无可查看报告">
        <el-button @click="goTo('/patient/visits')">查看就诊记录</el-button>
        <el-button type="primary" plain @click="goTo('/patient/appointments')">去预约挂号</el-button>
      </el-empty>
    </el-card>

    <el-dialog v-model="dialogVisible" title="报告预览" width="560px">
      <el-descriptions v-if="activeReport" :column="1" border>
        <el-descriptions-item label="报告编号">{{ activeReport.reportNo }}</el-descriptions-item>
        <el-descriptions-item label="项目">{{ activeReport.item }}</el-descriptions-item>
        <el-descriptions-item label="关联预约单号">{{ activeReport.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="结果">{{ activeReport.resultLabel || activeReport.result }}</el-descriptions-item>
        <el-descriptions-item label="结论">{{ activeReport.summary }}</el-descriptions-item>
        <el-descriptions-item label="建议">{{ activeReport.advice }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchMyAppointments } from "../../services/patientAppointments";
import { fetchPatientVisits } from "../../services/doctorClinic.js";
import { buildPatientReports } from "../../services/patientReports";
import { getActivePatient } from "../../services/patientSession";

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

const router = useRouter();
const keyword = ref("");
const dialogVisible = ref(false);
const activeReport = ref(null);
const activePatient = ref(getActivePatient());
const loading = ref(false);
const reports = ref([]);

const filteredReports = computed(() => {
  const key = keyword.value.trim();
  if (!key) {
    return reports.value;
  }
  return reports.value.filter((report) =>
    [report.reportNo, report.item, report.department, report.doctorName, report.serialNumber, report.source].some((value) =>
      String(value || "").includes(key)
    )
  );
});

function reportTagType(report) {
  if (report.result === "ATTENTION") {
    return "warning";
  }
  if (report.result === "FOLLOW_UP" || report.resultLabel === "待复核") {
    return "info";
  }
  return "success";
}

function goTo(path) {
  router.push(path);
}

function preview(report) {
  activeReport.value = report;
  dialogVisible.value = true;
}

async function loadReports() {
  loading.value = true;
  activePatient.value = getActivePatient();
  try {
    const [appointments, visitRecords] = await Promise.all([fetchMyAppointments(), fetchPatientVisits()]);
    reports.value = buildPatientReports({ appointments, visitRecords, today: getToday() });
  } catch (error) {
    reports.value = [];
    ElMessage.error(error.message || "报告加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadReports();
});
</script>

<style scoped>
.reports-page {
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
