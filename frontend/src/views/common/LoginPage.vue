<template>
  <div class="login-page welcome-orbit">
    <div class="orbit-glow orbit-glow-left" />
    <div class="orbit-glow orbit-glow-right" />
    <div class="orbit-grid" />

    <div class="login-shell">
      <section class="login-hero">
        <div class="hero-topline">
          <p class="eyebrow">Hospital Demo P0</p>
          <span class="hero-pill">暖色医疗中枢版</span>
        </div>
        <h1>医院业务系统演示登录</h1>
        <p class="hero-copy">
          这一版已经接入了最小鉴权、数据库持久化、真实排班管理，以及管理端组织与人员维护。
          患者除了使用演示账号，也可以在右侧直接完成自助注册，注册成功后会自动进入患者端首页。
        </p>

        <div class="hero-metrics">
          <article class="metric-card">
            <span class="metric-label">角色入口</span>
            <strong>3 端联动</strong>
            <p>患者、医生、管理员共享同一套真实演示链路。</p>
          </article>
          <article class="metric-card">
            <span class="metric-label">演示重点</span>
            <strong>预约到接诊</strong>
            <p>从挂号、支付、接诊到收费与药房均已打通主流程。</p>
          </article>
          <article class="metric-card">
            <span class="metric-label">体验方式</span>
            <strong>即点即用</strong>
            <p>点击下方账号卡即可一键填充演示账户，快速进入对应工作台。</p>
          </article>
        </div>

        <div class="preset-section">
          <div class="section-heading">
            <span>快速体验账号</span>
            <small>点击即可填充右侧登录表单</small>
          </div>
          <div class="preset-list">
            <button
              v-for="preset in presets"
              :key="preset.username"
              type="button"
              class="preset-card"
              @click="fillPreset(preset)"
            >
              <span class="preset-role">{{ preset.label }}</span>
              <strong>{{ preset.username }}</strong>
              <span class="preset-password">{{ preset.password }}</span>
            </button>
          </div>
        </div>
      </section>

      <el-card class="login-card" shadow="never">
        <template #header>
          <div class="card-header-shell">
            <div>
              <div class="card-title">账号登录</div>
              <div class="card-subtitle">输入账号后进入对应角色工作台，患者也可先注册再登录</div>
            </div>
            <span class="card-chip">安全接入</span>
          </div>
        </template>

        <el-alert
          title="可直接点击左侧演示账号自动填充，也可以使用管理端新建账号或患者自助注册后的账号登录。"
          type="info"
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
        <el-form-item label="显示名">
          <el-input v-model="registerForm.displayName" placeholder="请输入显示名" />
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
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login, registerPatient } from "../../services/auth";
import { roleMeta } from "../../config/menu";
import { setRegistrationOnboarding } from "../../services/patientSession";

const router = useRouter();
const route = useRoute();
const submitting = ref(false);
const registering = ref(false);
const registerVisible = ref(false);

const presets = [
  { label: "患者端", username: "patient", password: "patient123", role: "patient" },
  { label: "医生端", username: "doctor", password: "doctor123", role: "doctor" },
  { label: "管理端", username: "admin", password: "admin123", role: "admin" },
];

const form = reactive({
  username: "patient",
  password: "patient123",
});

const registerForm = reactive(createRegisterForm());

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
      ElMessage.success(`注册成功，已自动进入患者端，患者编号：${user.patientId || response.patientId}`);
      await router.replace(resolveTargetPath(user.role));
    } catch (loginError) {
      ElMessage.warning(`注册成功，患者编号：${response.patientId}，账号已创建，请手动登录`);
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

.hero-topline {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
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
  max-width: 620px;
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

.preset-section {
  margin-top: 26px;
}

.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  color: var(--orbit-ink);
}

.section-heading span {
  font-size: 18px;
  font-weight: 700;
}

.section-heading small {
  font-size: 12px;
  color: var(--orbit-muted);
}

.preset-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.preset-card {
  padding: 20px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 22px;
  background: linear-gradient(145deg, #ffffff 0%, #f7faf9 100%);
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: left;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.preset-card:hover {
  transform: translateY(-3px);
  border-color: rgba(15, 118, 110, 0.3);
  box-shadow: 0 18px 34px rgba(15, 118, 110, 0.1);
}

.preset-role,
.preset-password {
  font-size: 12px;
  color: #64748b;
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
  .preset-card {
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

  .hero-metrics,
  .preset-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .login-hero {
    padding: 26px;
  }

  .card-header-shell,
  .section-heading {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
