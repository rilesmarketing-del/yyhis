<template>
  <div class="dashboard-page patient-warmth-panel">
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
      <div class="hero-shell">
        <div class="hero-copy-block">
          <p class="hero-eyebrow">Patient Hub</p>
          <div class="panel-title">患者首页</div>
          <div class="panel-subtitle">围绕当前患者真实预约与就诊记录生成的温和型总览。</div>

          <div class="hero-badges">
            <span class="hero-badge">今日关注</span>
            <span class="hero-badge hero-badge-soft">安心就诊链路</span>
          </div>
        </div>

        <div class="hero-side-card">
          <span class="hero-side-label">当前登录患者</span>
          <strong>{{ activePatient.name }}</strong>
          <span>{{ activePatient.id }}</span>
          <el-button class="hero-refresh" @click="loadDashboard">刷新数据</el-button>
        </div>
      </div>

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
        <el-card shadow="never" class="section-card timeline-card" v-loading="loading">
          <template #header>
            <div class="header-row">
              <div>
                <span class="section-title">近期预约动态</span>
                <p class="section-subtitle">帮助患者快速确认下一次就诊、支付与后续检查状态。</p>
              </div>
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
        <el-card shadow="never" class="section-card action-card" v-loading="loading">
          <template #header>
            <div class="header-row compact-header">
              <div>
                <span class="section-title">快捷入口</span>
                <p class="section-subtitle">从首页直接进入预约、支付、就诊和报告查看。</p>
              </div>
            </div>
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
.patient-warmth-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-family: "Segoe UI Variable Text", "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

.welcome-card,
.hero-card,
.section-card {
  border: 1px solid rgba(141, 165, 159, 0.22);
  border-radius: 26px;
  box-shadow: 0 20px 42px rgba(15, 23, 42, 0.06);
}

.welcome-card {
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.16), transparent 28%),
    linear-gradient(135deg, #ffffff 0%, #f2fdf8 55%, #fff8ee 100%);
}

.welcome-shell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.welcome-eyebrow,
.hero-eyebrow {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #0f766e;
}

.welcome-title {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
}

.welcome-subtitle {
  margin-top: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #0f766e;
}

.welcome-desc {
  max-width: 620px;
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: #475569;
}

.welcome-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.welcome-primary,
.hero-refresh {
  min-width: 136px;
  min-height: 44px;
  border-radius: 14px;
}

.hero-card {
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(251, 191, 36, 0.16), transparent 24%),
    linear-gradient(140deg, #fefefe 0%, #eefbf8 48%, #fff7ed 100%);
}

.hero-shell {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.hero-copy-block {
  max-width: 620px;
}

.panel-title {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.panel-subtitle {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #55646f;
}

.hero-badges {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #134e4a;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.14), rgba(255, 255, 255, 0.9));
}

.hero-badge-soft {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.16), rgba(255, 255, 255, 0.9));
}

.hero-side-card {
  min-width: 220px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.68);
  backdrop-filter: blur(10px);
}

.hero-side-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

.hero-side-card strong {
  font-size: 22px;
  color: #0f172a;
}

.hero-side-card span:last-of-type {
  color: #64748b;
}

.patient-alert {
  margin-bottom: 16px;
  border-radius: 18px;
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

.compact-header {
  align-items: flex-start;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #183b35;
}

.section-subtitle {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: #64748b;
}

.stat-card {
  min-height: 152px;
  border-radius: 22px;
  border: 1px solid rgba(215, 239, 232, 0.72);
  background: rgba(255, 255, 255, 0.82);
}

.stat-label {
  color: #58716a;
  font-size: 13px;
}

.stat-value {
  margin-top: 10px;
  font-size: 32px;
  font-weight: 700;
  color: #0f766e;
}

.stat-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
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
  min-height: 112px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(255, 255, 255, 0.88);
  cursor: pointer;
  text-align: left;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.quick-action:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 28px rgba(15, 118, 110, 0.1);
}

.quick-action-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.quick-action-desc {
  font-size: 12px;
  line-height: 1.6;
  color: #64748b;
}

.quick-action-primary {
  border-color: rgba(191, 219, 254, 0.9);
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
}

.quick-action-success {
  border-color: rgba(187, 247, 208, 0.9);
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
}

.quick-action-warning {
  border-color: rgba(253, 230, 138, 0.9);
  background: linear-gradient(135deg, #fffbeb 0%, #ffffff 100%);
}

.quick-action-info {
  border-color: rgba(203, 213, 225, 0.9);
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.muted {
  margin-top: 4px;
  color: #6c7f7a;
}

@media (max-width: 900px) {
  .welcome-shell,
  .hero-shell,
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .welcome-primary,
  .hero-refresh {
    width: 100%;
  }

  .hero-side-card {
    width: 100%;
    min-width: 0;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
