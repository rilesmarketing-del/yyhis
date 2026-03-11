<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-hero">
        <p class="eyebrow">Hospital Demo P0</p>
        <h1>医院业务系统演示登录</h1>
        <p class="hero-copy">
          这一版已经接入了最小鉴权、数据库持久化、真实排班管理，以及管理端组织与人员维护。
          患者除了使用演示账号，也可以在右侧直接完成自助注册，再用新账号登录患者端。
        </p>

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
      </section>

      <el-card class="login-card" shadow="never">
        <template #header>
          <div>
            <div class="card-title">账号登录</div>
            <div class="card-subtitle">输入账号后进入对应角色工作台，患者也可先注册再登录</div>
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
        <el-button type="primary" :loading="registering" @click="handleRegister">注册并返回登录</el-button>
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
  if (!registerForm.username.trim() || !registerForm.password.trim() || !registerForm.confirmPassword.trim()
    || !registerForm.displayName.trim() || !registerForm.mobile.trim()) {
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
      username: registerForm.username.trim(),
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      displayName: registerForm.displayName.trim(),
      mobile: registerForm.mobile.trim(),
    });
    form.username = registerForm.username.trim();
    form.password = registerForm.password;
    registerVisible.value = false;
    ElMessage.success(`注册成功，患者编号 ${response.patientId}，请使用新账号登录`);
  } catch (error) {
    ElMessage.error(error.message || "注册失败，请稍后重试");
  } finally {
    registering.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(20, 184, 166, 0.2), transparent 28%),
    radial-gradient(circle at bottom right, rgba(249, 115, 22, 0.18), transparent 26%),
    linear-gradient(135deg, #f8fafc 0%, #eefbf8 52%, #fff7ed 100%);
}

.login-shell {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 420px);
  gap: 24px;
  align-items: stretch;
}

.login-hero,
.login-card {
  border-radius: 24px;
}

.login-hero {
  padding: 36px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.22);
  backdrop-filter: blur(16px);
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

.login-hero h1 {
  margin: 14px 0 12px;
  font-size: clamp(32px, 4vw, 52px);
  line-height: 1.05;
  color: #0f172a;
}

.hero-copy {
  max-width: 580px;
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #475569;
}

.preset-list {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.preset-card {
  padding: 18px;
  border: 1px solid #dbeafe;
  border-radius: 18px;
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.preset-card:hover {
  transform: translateY(-2px);
  border-color: #14b8a6;
  box-shadow: 0 12px 30px rgba(15, 118, 110, 0.08);
}

.preset-role,
.preset-password {
  font-size: 12px;
  color: #64748b;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.card-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
}

.login-alert {
  margin-bottom: 18px;
}

.login-button {
  width: 100%;
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

@media (max-width: 900px) {
  .login-page {
    padding: 18px;
  }

  .login-shell {
    grid-template-columns: 1fr;
  }

  .preset-list {
    grid-template-columns: 1fr;
  }
}
</style>