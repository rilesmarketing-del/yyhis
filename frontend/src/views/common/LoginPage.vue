<template>
  <div class="login-page welcome-orbit">
    <div class="orbit-glow orbit-glow-left" />
    <div class="orbit-glow orbit-glow-right" />
    <div class="orbit-grid" />

    <div class="login-shell" :class="{ 'patient-mode-shell': patientModeEnabled }">
      <section class="login-hero" :class="{ 'patient-mode-banner': patientModeEnabled }">
        <div class="hero-topline" :class="{ 'patient-mode-topline': patientModeEnabled }">
          <p class="eyebrow">智慧医院业务系统</p>
          <span class="hero-pill">门诊服务与临床协同平台</span>
          <el-button
            v-if="patientModeEnabled"
            plain
            class="mode-toggle-button mode-toggle-inline"
            @click="unlockVisible = true"
          >
            切换模式
          </el-button>
        </div>
        <h1>{{ patientModeEnabled ? "医院患者服务入口" : "医院业务系统登录" }}</h1>
        <p v-if="!patientModeEnabled" class="hero-copy">面向患者服务、临床接诊和运营管理的一体化系统，支持预约、接诊、收费、药房和组织治理等核心业务协同。</p>

        <div v-if="!patientModeEnabled" class="hero-metrics role-entry-grid">
          <article class="metric-card">
            <span class="metric-label">患者服务入口</span>
            <strong>预约与就诊</strong>
            <p>用于患者预约挂号、支付、查看就诊记录、报告与处方。</p>
            <button type="button" class="entry-shortcut" @click="fillPreset(patientPreset)">填入患者账号</button>
          </article>
          <article class="metric-card">
            <span class="metric-label">医护工作入口</span>
            <strong>接诊与病历</strong>
            <p>用于医护人员查看候诊队列、接诊、记录医嘱与维护病历。</p>
            <button type="button" class="entry-shortcut" @click="fillPreset(doctorPreset)">填入医护账号</button>
          </article>
          <article class="metric-card">
            <span class="metric-label">运营管理入口</span>
            <strong>排班与治理</strong>
            <p>用于管理人员处理排班、收费、药房、报表和组织治理工作。</p>
            <button type="button" class="entry-shortcut" @click="fillPreset(adminPreset)">填入管理账号</button>
          </article>
        </div>

        <div class="mode-actions" v-if="!patientModeEnabled">
          <el-button plain class="mode-toggle-button" @click="handleEnablePatientMode">
            切换为患者模式
          </el-button>
        </div>
      </section>

      <el-card class="login-card" :class="{ 'patient-login-card': patientModeEnabled }" shadow="never">
        <template #header>
          <div class="card-header-shell">
            <div>
              <div class="card-title">{{ patientModeEnabled ? "患者登录" : "账号登录" }}</div>
              <div class="card-subtitle">
                {{ patientModeEnabled ? "当前仅开放患者登录与患者自助注册。" : "请输入账号密码进入对应业务入口。" }}
              </div>
            </div>
            <span class="card-chip">安全接入</span>
          </div>
        </template>

        <el-alert
          v-if="!patientModeEnabled"
          :title="patientModeEnabled ? '当前浏览器处于患者服务模式。若需恢复后台入口，请由管理员验证后解除。' : '请使用对应业务账号登录。患者也可以直接完成自助注册后进入系统。'"
          :type="patientModeEnabled ? 'warning' : 'info'"
          :closable="false"
          show-icon
          class="login-alert"
        />

        <el-form label-position="top" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-button" :loading="submitting" @click="handleLogin">
              登录系统
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button text class="register-button" @click="openRegisterDialog">患者自助注册</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-dialog v-model="registerVisible" title="患者自助注册" width="520px">
      <el-form :model="registerForm" label-width="92px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="请输入唯一用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="患者名称">
          <el-input v-model="registerForm.displayName" placeholder="请输入患者名称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.mobile" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="handleRegister">注册并进入患者端</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="unlockVisible" title="切换模式" width="440px" class="unlock-admin-dialog">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="管理员账号">
          <el-input v-model="unlockForm.username" placeholder="请输入管理员用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="管理员密码">
          <el-input
            v-model="unlockForm.password"
            type="password"
            show-password
            placeholder="请输入管理员密码"
            autocomplete="current-password"
            @keyup.enter="handleDisablePatientMode"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unlockVisible = false">取消</el-button>
        <el-button type="primary" :loading="unlocking" @click="handleDisablePatientMode">切换模式</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { clearAuthSession, login, loginForVerification, registerPatient } from "../../services/auth";
import { roleMeta } from "../../config/menu";
import { setRegistrationOnboarding } from "../../services/patientSession";
import { disablePatientMode, enablePatientMode, isPatientModeEnabled } from "../../services/patientMode";

const router = useRouter();
const route = useRoute();
const submitting = ref(false);
const registering = ref(false);
const registerVisible = ref(false);
const unlocking = ref(false);
const unlockVisible = ref(false);
const patientModeState = ref(isPatientModeEnabled());

const patientPreset = { label: "患者服务入口", username: "patient", password: "patient123", role: "patient" };
const doctorPreset = { label: "医护工作入口", username: "doctor", password: "doctor123", role: "doctor" };
const adminPreset = { label: "运营管理入口", username: "admin", password: "admin123", role: "admin" };

const patientModeEnabled = computed(() => patientModeState.value);

const form = reactive({
  username: "patient",
  password: "patient123",
});

const registerForm = reactive(createRegisterForm());
const unlockForm = reactive({
  username: "",
  password: "",
});

function createRegisterForm() {
  return {
    username: "",
    password: "",
    confirmPassword: "",
    displayName: "",
    mobile: "",
  };
}

function fillPreset(preset) {
  form.username = preset.username;
  form.password = preset.password;
}

function fillLoginForm(username, password) {
  form.username = username;
  form.password = password;
}

function resetUnlockForm() {
  unlockForm.username = "";
  unlockForm.password = "";
}

function syncPatientModeState() {
  patientModeState.value = isPatientModeEnabled();
}

function openRegisterDialog() {
  Object.assign(registerForm, createRegisterForm());
  registerVisible.value = true;
}

function resolveTargetPath(role) {
  const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "";
  if (redirect && redirect.startsWith(`/${role}/`)) {
    return redirect;
  }
  return roleMeta[role]?.homePath || "/";
}

function handleEnablePatientMode() {
  enablePatientMode();
  clearAuthSession();
  syncPatientModeState();
  fillPreset(patientPreset);
  ElMessage.success("当前浏览器已切换为患者模式");
}

async function handleDisablePatientMode() {
  if (!unlockForm.username.trim() || !unlockForm.password.trim()) {
    ElMessage.warning("请输入管理员账号和密码");
    return;
  }

  unlocking.value = true;
  try {
    const session = await loginForVerification({
      username: unlockForm.username.trim(),
      password: unlockForm.password,
    });
    if (session.currentUser?.role !== "admin") {
      ElMessage.error("只有管理员账号可以切换模式");
      return;
    }

    disablePatientMode();
    clearAuthSession();
    syncPatientModeState();
    resetUnlockForm();
    unlockVisible.value = false;
    ElMessage.success("患者模式已解除，系统已恢复完整入口");
  } catch (error) {
    ElMessage.error(error.message || "管理员验证失败，请稍后重试");
  } finally {
    unlocking.value = false;
  }
}

async function handleLogin() {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning("请输入用户名和密码");
    return;
  }

  submitting.value = true;
  try {
    const user = await login({
      username: form.username.trim(),
      password: form.password,
    });
    if (patientModeEnabled.value && ["doctor", "admin"].includes(user.role)) {
      clearAuthSession();
      ElMessage.error("当前浏览器处于患者模式，暂不开放该账号入口");
      return;
    }
    ElMessage.success(`欢迎回来，${user.displayName}`);
    await router.replace(resolveTargetPath(user.role));
  } catch (error) {
    ElMessage.error(error.message || "登录失败，请稍后重试");
  } finally {
    submitting.value = false;
  }
}

async function handleRegister() {
  const username = registerForm.username.trim();
  const password = registerForm.password;
  const displayName = registerForm.displayName.trim();
  const mobile = registerForm.mobile.trim();

  if (!username || !password || !registerForm.confirmPassword.trim() || !displayName || !mobile) {
    ElMessage.warning("请完整填写注册信息");
    return;
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning("两次输入的密码不一致");
    return;
  }

  registering.value = true;
  try {
    const response = await registerPatient({
      username,
      password,
      confirmPassword: registerForm.confirmPassword,
      displayName,
      mobile,
    });

    fillLoginForm(username, password);
    registerVisible.value = false;
    Object.assign(registerForm, createRegisterForm());

    try {
      const user = await login({ username, password });
      setRegistrationOnboarding({
        username: user.username || username,
        displayName: user.displayName || response.displayName || displayName,
        patientId: user.patientId || response.patientId,
      });
      ElMessage.success("注册成功，已自动进入患者端");
      await router.replace(resolveTargetPath(user.role));
    } catch (loginError) {
      ElMessage.warning("注册成功，账号已创建，请手动登录");
    }
  } catch (error) {
    ElMessage.error(error.message || "注册失败，请稍后重试");
  } finally {
    registering.value = false;
  }
}
</script>

<style scoped>
.welcome-orbit {
  --orbit-teal: #0f766e;
  --orbit-amber: #f59e0b;
  --orbit-ink: #0f172a;
  --orbit-muted: #52626c;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  padding: 34px;
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.18), transparent 26%),
    radial-gradient(circle at 85% 18%, rgba(251, 191, 36, 0.2), transparent 22%),
    radial-gradient(circle at 76% 78%, rgba(56, 189, 248, 0.12), transparent 24%),
    linear-gradient(135deg, #f8fafc 0%, #eefbf8 48%, #fff7ed 100%);
  font-family: "Segoe UI Variable Text", "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

.orbit-glow,
.orbit-grid {
  position: absolute;
  pointer-events: none;
}

.orbit-glow {
  border-radius: 999px;
  filter: blur(20px);
}

.orbit-glow-left {
  top: 82px;
  left: -24px;
  width: 220px;
  height: 220px;
  background: rgba(45, 212, 191, 0.22);
}

.orbit-glow-right {
  right: 36px;
  bottom: 48px;
  width: 240px;
  height: 240px;
  background: rgba(251, 191, 36, 0.16);
}

.orbit-grid {
  inset: 0;
  opacity: 0.3;
  background-image:
    linear-gradient(rgba(15, 118, 110, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 118, 110, 0.04) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(circle at center, rgba(0, 0, 0, 0.8), transparent 78%);
}

.login-shell {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(340px, 420px);
  gap: 28px;
  align-items: stretch;
}

.patient-mode-shell {
  --patient-mode-inline: 32px;
  max-width: 760px;
  grid-template-columns: minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.patient-mode-shell > * {
  width: 100%;
}

.login-hero,
.login-card {
  border-radius: 30px;
}

.login-hero {
  padding: 38px;
  position: relative;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(141, 165, 159, 0.24);
  box-shadow: 0 28px 64px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
  animation: rise-in 220ms ease-out;
}

.patient-mode-banner {
  background: linear-gradient(145deg, rgba(255, 251, 235, 0.92), rgba(240, 253, 250, 0.9));
  width: 100%;
  box-sizing: border-box;
  padding: 28px var(--patient-mode-inline);
}

.patient-mode-banner h1 {
  margin: 14px 0 0;
  font-size: clamp(28px, 4vw, 40px);
}

.patient-mode-banner .mode-actions {
  margin-top: 16px;
}

.hero-topline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.patient-mode-topline {
  align-items: center;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--orbit-teal);
}

.hero-pill,
.card-chip {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #134e4a;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.14), rgba(245, 158, 11, 0.2));
  border: 1px solid rgba(15, 118, 110, 0.08);
}

.login-hero h1 {
  max-width: 640px;
  margin: 18px 0 14px;
  font-size: clamp(34px, 4.3vw, 56px);
  line-height: 0.98;
  letter-spacing: -0.03em;
  color: var(--orbit-ink);
}

.hero-copy {
  max-width: 650px;
  margin: 0;
  font-size: 16px;
  line-height: 1.85;
  color: #435562;
}

.hero-metrics {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.role-entry-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-card {
  min-height: 156px;
  padding: 20px;
  border-radius: 22px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.94), rgba(245, 248, 247, 0.9));
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.metric-label {
  display: inline-flex;
  margin-bottom: 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--orbit-teal);
}

.metric-card strong {
  display: block;
  font-size: 22px;
  color: #10231f;
}

.metric-card p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--orbit-muted);
}

.entry-shortcut {
  margin-top: 16px;
  min-height: 42px;
  padding: 0 16px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.14), rgba(245, 158, 11, 0.18));
  color: #134e4a;
  font-weight: 700;
  cursor: pointer;
}

.mode-actions {
  margin-top: 20px;
}

.mode-toggle-button {
  min-height: 44px;
  border-radius: 14px;
}

.mode-toggle-inline {
  margin-left: auto;
}

.card-header-shell {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.login-card {
  border: 1px solid rgba(141, 165, 159, 0.2);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.1);
  backdrop-filter: blur(18px);
}

.patient-login-card {
  width: 100%;
  min-height: auto;
  box-sizing: border-box;
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--orbit-ink);
}

.card-subtitle {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
}

.login-alert {
  margin-bottom: 18px;
  border-radius: 18px;
}

.login-button {
  width: 100%;
  min-height: 46px;
  border: none;
  border-radius: 16px;
  box-shadow: 0 16px 24px rgba(15, 118, 110, 0.18);
}

.register-button {
  width: 100%;
  justify-content: center;
}

:deep(.login-card .el-card__body) {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 100%;
}

:deep(.patient-login-card .el-card__body) {
  justify-content: flex-start;
  padding: 0 var(--patient-mode-inline) 26px;
}

:deep(.patient-login-card .el-card__header) {
  padding: 28px var(--patient-mode-inline) 18px;
}

:deep(.login-card .el-input__wrapper) {
  min-height: 46px;
  border-radius: 14px;
}

:deep(.el-dialog) {
  border-radius: 28px;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-hero,
  .metric-card,
  .entry-shortcut {
    animation: none;
    transition: none;
  }
}

@media (max-width: 980px) {
  .welcome-orbit {
    padding: 20px;
  }

  .login-shell,
  .patient-mode-shell {
    grid-template-columns: 1fr;
  }

  .hero-metrics,
  .role-entry-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .login-hero {
    padding: 26px;
  }

  .card-header-shell {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
