<template>
  <div class="dashboard-page">
    <el-card v-if="registrationGuide" class="welcome-card" shadow="never">
      <div class="welcome-shell">
        <div>
          <p class="welcome-eyebrow">新患者引导</p>
          <div class="welcome-title">{{ registrationGuide.title }}</div>
          <div class="welcome-subtitle">{{ registrationGuide.patientLabel }}</div>
          <p class="welcome-desc">{{ registrationGuide.description }}</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" class="welcome-primary" @click="handleGuidePrimaryAction">
            {{ registrationGuide.primaryAction.label }}
          </el-button>
          <el-button text @click="dismissRegistrationGuide">{{ registrationGuide.secondaryAction.label }}</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="hero-card" shadow="never">
      <template #header>
        <div class="header-row">
          <div>
            <div class="panel-title">患者首页</div>
            <div class="panel-subtitle">基于当前患者真实预约数据生成的总览</div>
          </div>
          <el-button @click="loadDashboard">刷新数据</el-button>
        </div>
      </template>

      <el-alert
        :title="`当前登录患者：${activePatient.name}（${activePatient.id}）`"
        type="success"
        :closable="false"
        show-icon
        class="patient-alert"
      />

      <el-row :gutter="12">
        <el-col v-for="card in dashboardModel.stats" :key="card.label" :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card" shadow="hover" v-loading="loading">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-desc">{{ card.desc }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <div class="header-row">
              <span>近期预约动态</span>
              <el-button link type="primary" @click="goTo('/patient/appointments')">查看全部</el-button>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="item in dashboardModel.timeline"
              :key="item.id"
              :timestamp="item.time"
              :type="item.type"
            >
              <strong>{{ item.title }}</strong>
              <div class="muted">{{ item.desc }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <span>快捷入口</span>
          </template>

          <div class="quick-actions">
            <button
              v-for="action in dashboardModel.quickActions"
              :key="action.key"
              type="button"
              class="quick-action"
              :class="`quick-action-${action.type}`"
              @click="goTo(action.path)"
            >
              <span class="quick-action-title">{{ action.label }}</span>
              <span class="quick-action-desc">{{ action.desc }}</span>
            </button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchMyAppointments } from "../../services/patientAppointments";
import { buildPatientDashboardModel, buildPatientRegistrationGuide } from "../../services/patientDashboard";
import {
  clearRegistrationOnboarding,
  getActivePatient,
  getRegistrationOnboarding,
} from "../../services/patientSession";

function getToday() {
  return new Date().toISOString().slice(0, 10);
}

function getCurrentMonth() {
  return new Date().toISOString().slice(0, 7);
}

const router = useRouter();
const activePatient = ref(getActivePatient());
const loading = ref(false);
const registrationGuide = ref(null);
const dashboardModel = ref(
  buildPatientDashboardModel({
    appointments: [],
    today: getToday(),
    currentMonth: getCurrentMonth(),
  })
);

function consumeRegistrationGuide() {
  const onboarding = getRegistrationOnboarding();
  registrationGuide.value = buildPatientRegistrationGuide(onboarding);
  if (onboarding) {
    clearRegistrationOnboarding();
  }
}

function dismissRegistrationGuide() {
  registrationGuide.value = null;
}

function handleGuidePrimaryAction() {
  const targetPath = registrationGuide.value?.primaryAction?.path;
  registrationGuide.value = null;
  if (targetPath) {
    router.push(targetPath);
  }
}

function goTo(path) {
  router.push(path);
}

async function loadDashboard() {
  loading.value = true;
  activePatient.value = getActivePatient();
  try {
    const appointments = await fetchMyAppointments();
    dashboardModel.value = buildPatientDashboardModel({
      appointments,
      today: getToday(),
      currentMonth: getCurrentMonth(),
    });
  } catch (error) {
    dashboardModel.value = buildPatientDashboardModel({
      appointments: [],
      today: getToday(),
      currentMonth: getCurrentMonth(),
    });
    ElMessage.error(error.message || "首页数据加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  consumeRegistrationGuide();
  loadDashboard();
});
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.welcome-card,
.hero-card {
  border: 1px solid #dbeafe;
}

.welcome-card {
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.18), transparent 26%),
    linear-gradient(135deg, #ffffff 0%, #f0fdf9 55%, #fff7ed 100%);
}

.welcome-shell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.welcome-eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #0f766e;
}

.welcome-title {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.welcome-subtitle {
  margin-top: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #0f766e;
}

.welcome-desc {
  max-width: 620px;
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: #475569;
}

.welcome-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.welcome-primary {
  min-width: 132px;
}

.hero-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 55%, #ecfeff 100%);
}

.mt-12 {
  margin-top: 12px;
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

.stat-card {
  min-height: 148px;
  border-radius: 16px;
  border: 1px solid #d7efe8;
  background: rgba(255, 255, 255, 0.88);
}

.stat-label {
  color: #5a6b67;
  font-size: 13px;
}

.stat-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 700;
  color: #0f766e;
}

.stat-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  min-height: 104px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #dbeafe;
  background: #ffffff;
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.quick-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 118, 110, 0.08);
}

.quick-action-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.quick-action-desc {
  font-size: 12px;
  line-height: 1.5;
  color: #64748b;
}

.quick-action-primary {
  border-color: #bfdbfe;
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
}

.quick-action-success {
  border-color: #bbf7d0;
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
}

.quick-action-warning {
  border-color: #fde68a;
  background: linear-gradient(135deg, #fffbeb 0%, #ffffff 100%);
}

.quick-action-info {
  border-color: #cbd5e1;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.muted {
  color: #6c7f7a;
  margin-top: 3px;
}

@media (max-width: 900px) {
  .welcome-shell,
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome-actions {
    width: 100%;
  }

  .welcome-primary {
    width: 100%;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>