<template>
  <div class="login-page welcome-orbit" :class="{ 'senior-care-preview': patientModeEnabled && seniorCareModeEnabled }">
    <div class="orbit-glow orbit-glow-left" />
    <div class="orbit-glow orbit-glow-right" />
    <div class="orbit-grid" />

    <div class="login-shell" :class="{ 'patient-mode-shell': patientModeEnabled }">
      <section class="login-hero" :class="{ 'patient-mode-banner': patientModeEnabled }">
        <div class="hero-topline" :class="{ 'patient-mode-topline': patientModeEnabled }">
          <p class="eyebrow">智慧医院业务系统</p>
          <span class="hero-pill">门诊服务与临床协同平台</span>
          <el-button
            plain
            class="mode-toggle-button mode-toggle-inline"
            @click="handleModeToggleAction"
          >
            {{ patientModeEnabled ? "切换模式" : "切换为患者模式" }}
          </el-button>
        </div>
        <h1>{{ patientModeEnabled ? "医院患者服务入口" : "医院管理后台登录" }}</h1>
        <p class="hero-copy">
          {{
            patientModeEnabled
              ? "当前浏览器已切换为患者服务入口，适用于患者登录与自助注册。"
              : "面向组织治理、排班配置、收费管理和运营分析的统一后台入口。"
          }}
        </p>
      </section>

      <el-card class="login-card" :class="{ 'patient-login-card': patientModeEnabled }" shadow="never">
        <template #header>
          <div class="card-header-shell">
            <div>
              <div class="card-title">{{ patientModeEnabled ? "患者登录" : "管理员登录" }}</div>
              <div class="card-subtitle">
                {{ patientModeEnabled ? "当前仅开放患者登录与患者自助注册。" : "请输入管理员账号密码进入管理后台。" }}
              </div>
            </div>
          </div>
        </template>

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
          <el-form-item v-if="!patientModeEnabled" class="remember-password-item remember-password-row">
            <label class="remember-checkbox-native">
              <input v-model="rememberPassword" type="checkbox" class="remember-checkbox-input" />
              <span class="remember-checkbox-box" aria-hidden="true" />
              <span class="remember-checkbox-text">记住密码</span>
            </label>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-button" :loading="submitting" @click="handleLogin">
              登录系统
            </el-button>
          </el-form-item>
          <el-form-item v-if="patientModeEnabled">
            <el-button text class="register-button" @click="openRegisterDialog">患者自助注册</el-button>
          </el-form-item>
          <el-form-item v-if="patientModeEnabled" class="senior-care-item">
            <div class="senior-care-toggle-row">
              <div class="senior-care-copy">
                <div class="senior-care-title">长者关怀模式</div>
              </div>
              <el-button class="senior-care-toggle-button" :class="{ 'is-active': seniorCareModeEnabled }" @click="toggleSeniorCareMode">
                {{ seniorCareModeEnabled ? "关闭" : "开启" }}
              </el-button>
            </div>
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
import { disableSeniorCareMode, enableSeniorCareMode, isSeniorCareModeEnabled } from "../../services/seniorCareMode";
import {
  clearRememberedBackofficeCredentials,
  getRememberedBackofficeCredentials,
  setRememberedBackofficeCredentials,
} from "../../services/loginMemory";

const router = useRouter();
const route = useRoute();
const submitting = ref(false);
const registering = ref(false);
const registerVisible = ref(false);
const unlocking = ref(false);
const unlockVisible = ref(false);
const patientModeState = ref(isPatientModeEnabled());
const seniorCareModeEnabled = ref(isSeniorCareModeEnabled());
const rememberedBackofficeCredentials = getRememberedBackofficeCredentials();

const patientPreset = { label: "患者服务入口", username: "patient", password: "patient123", role: "patient" };

const patientModeEnabled = computed(() => patientModeState.value);
const adminPreset = { label: "管理后台入口", username: "admin", password: "admin123", role: "admin" };

function getNormalModeLoginDefaults() {
  return {
    username: rememberedBackofficeCredentials.username || adminPreset.username,
    password: rememberedBackofficeCredentials.password || adminPreset.password,
  };
}

const form = reactive({
  ...(patientModeState.value
    ? { username: patientPreset.username, password: patientPreset.password }
    : getNormalModeLoginDefaults()),
});
const rememberPassword = ref(!patientModeState.value && rememberedBackofficeCredentials.rememberPassword !== false);

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

function fillLoginForm(username, password) {
  form.username = username;
  form.password = password;
}

function applyNormalModeDefaults() {
  const defaults = getRememberedBackofficeCredentials();
  fillLoginForm(defaults.username || adminPreset.username, defaults.password || adminPreset.password);
  rememberPassword.value = defaults.rememberPassword !== false;
}

function resetUnlockForm() {
  unlockForm.username = "";
  unlockForm.password = "";
}

function syncPatientModeState() {
  patientModeState.value = isPatientModeEnabled();
}

function handleSeniorCareToggle(enabled) {
  seniorCareModeEnabled.value = enabled;

  if (enabled) {
    enableSeniorCareMode();
    return;
  }

  disableSeniorCareMode();
}

function toggleSeniorCareMode() {
  handleSeniorCareToggle(!seniorCareModeEnabled.value);
}

function openRegisterDialog() {
  if (!patientModeEnabled.value) {
    return;
  }

  Object.assign(registerForm, createRegisterForm());
  registerVisible.value = true;
}

function handleModeToggleAction() {
  if (patientModeEnabled.value) {
    unlockVisible.value = true;
    return;
  }

  handleEnablePatientMode();
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
  form.username = patientPreset.username;
  form.password = patientPreset.password;
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
    disableSeniorCareMode();
    clearAuthSession();
    syncPatientModeState();
    seniorCareModeEnabled.value = false;
    applyNormalModeDefaults();
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
    if (patientModeEnabled.value && user.role === "patient") {
      if (seniorCareModeEnabled.value) {
        enableSeniorCareMode();
      } else {
        disableSeniorCareMode();
      }
    } else {
      disableSeniorCareMode();
    }
    if (!patientModeEnabled.value) {
      if (rememberPassword.value) {
        setRememberedBackofficeCredentials({
          username: form.username.trim(),
          password: form.password,
          rememberPassword: true,
        });
      } else {
        clearRememberedBackofficeCredentials();
      }
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
      if (seniorCareModeEnabled.value) {
        enableSeniorCareMode();
      } else {
        disableSeniorCareMode();
      }
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
  --login-shell-inline: 32px;
  position: relative;
  z-index: 1;
  max-width: 760px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.login-shell > * {
  width: 100%;
}

.login-hero,
.login-card {
  border-radius: 30px;
}

.login-hero {
  width: 100%;
  box-sizing: border-box;
  padding: 28px var(--login-shell-inline);
  position: relative;
  background: linear-gradient(145deg, rgba(255, 251, 235, 0.92), rgba(240, 253, 250, 0.9));
  border: 1px solid rgba(141, 165, 159, 0.24);
  box-shadow: 0 28px 64px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
  animation: rise-in 220ms ease-out;
}

.patient-mode-banner {
  padding: 28px var(--login-shell-inline);
}

.patient-mode-banner h1 {
  margin: 14px 0 0;
  font-size: clamp(28px, 4vw, 40px);
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

.hero-pill {
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

.senior-care-item {
  margin-top: -4px;
  margin-bottom: 0;
}

.senior-care-toggle-row {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(15, 118, 110, 0.14);
  background: linear-gradient(135deg, rgba(240, 253, 250, 0.92), rgba(255, 251, 235, 0.88));
}

.senior-care-copy {
  display: flex;
  flex-direction: column;
}

.senior-care-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--orbit-ink);
}

.senior-care-toggle-button {
  min-width: 96px;
  min-height: 42px;
  padding-inline: 18px;
  border-radius: 999px;
  border: 1px solid rgba(15, 118, 110, 0.14);
  background: rgba(255, 255, 255, 0.94);
  color: var(--orbit-teal-deep);
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.senior-care-toggle-button.is-active {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--orbit-teal), #14b8a6);
}

.remember-password-item {
  margin-top: -4px;
  margin-bottom: 8px;
}

.remember-password-row {
  display: flex;
  justify-content: flex-start;
}

.remember-checkbox-native {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--orbit-ink);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
}

.remember-checkbox-input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.remember-checkbox-box {
  position: relative;
  flex: 0 0 auto;
  width: 16px;
  height: 16px;
  margin-top: 0;
  border: 2px solid rgba(15, 23, 42, 0.28);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
  transition:
    border-color 140ms ease,
    background 140ms ease,
    box-shadow 140ms ease,
    transform 140ms ease;
}

.remember-checkbox-box::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  width: 4px;
  height: 8px;
  border-right: 2px solid #ffffff;
  border-bottom: 2px solid #ffffff;
  opacity: 0;
  transform: translate(-50%, -50%) rotate(45deg) scale(0.8);
  transition:
    opacity 140ms ease,
    transform 140ms ease;
}

.remember-checkbox-input:checked + .remember-checkbox-box {
  background: linear-gradient(135deg, var(--orbit-teal), #14b8a6);
  border-color: var(--orbit-teal);
}

.remember-checkbox-input:checked + .remember-checkbox-box::after {
  opacity: 1;
  transform: translate(-50%, -50%) rotate(45deg) scale(1);
}

.remember-checkbox-native:hover .remember-checkbox-box,
.remember-checkbox-input:focus-visible + .remember-checkbox-box {
  border-color: rgba(15, 118, 110, 0.55);
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12);
}

.remember-checkbox-text {
  padding-left: 0;
  line-height: 1;
}

:deep(.login-card .el-card__body) {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 100%;
}

:deep(.patient-login-card .el-card__body) {
  justify-content: flex-start;
  padding: 0 var(--login-shell-inline) 26px;
}

:deep(.patient-login-card .el-card__header) {
  padding: 28px var(--login-shell-inline) 18px;
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
  .login-hero {
    animation: none;
    transition: none;
  }
}

@media (max-width: 980px) {
  .welcome-orbit {
    padding: 20px;
  }

  .login-shell {
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

  .senior-care-toggle-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
