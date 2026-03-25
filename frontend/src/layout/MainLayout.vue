<template>
  <el-container class="shell medical-hub-shell" :class="{ 'senior-care-mode': seniorCareEnabled }">
    <div class="shell-ambient ambient-one" />
    <div class="shell-ambient ambient-two" />
    <div class="shell-ambient ambient-three" />

    <el-header class="header">
      <div class="brand-shell">
        <div class="brand">
          <h1>智慧医院业务系统</h1>
          <p class="subtitle">门诊服务、临床接诊与运营管理协同平台</p>
        </div>
      </div>

      <div class="account-panel">
        <div class="account-meta">
          <span class="account-name">{{ displayName }}</span>
        </div>
        <el-button class="logout-button" type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="shell-body">
      <el-aside width="268px" class="aside">
        <div class="aside-shell">
          <el-menu
            :default-active="activePath"
            class="menu"
            router
            background-color="transparent"
            text-color="#33514c"
            active-text-color="#0f766e"
          >
            <el-menu-item v-for="item in currentMenus" :key="item.path" :index="item.path">
              <el-icon v-if="item.icon">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-aside>

      <el-main class="main">
        <el-card class="breadcrumb" shadow="never">
          <div class="breadcrumb-shell">
            <div>
              <p class="breadcrumb-label">当前位置</p>
              <el-breadcrumb separator="/">
                <el-breadcrumb-item>{{ roleLabel }}</el-breadcrumb-item>
                <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="breadcrumb-pill">{{ currentTitle }}</div>
          </div>
        </el-card>

        <section class="router-view-shell">
          <router-view />
        </section>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { roleMenus, roleMeta } from "../config/menu";
import { authState, logout } from "../services/auth";
import { disableSeniorCareMode, isSeniorCareModeEnabled } from "../services/seniorCareMode";

const route = useRoute();
const router = useRouter();

const currentRole = computed(() => route.meta?.role || route.path.split("/")[1] || authState.currentUser?.role || "patient");
const currentUser = computed(() => authState.currentUser);
const currentMenus = computed(() => roleMenus[currentRole.value] || []);
const activePath = computed(() => route.path);
const roleLabel = computed(() => roleMeta[currentRole.value]?.label || "未知角色");
const currentTitle = computed(() => route.meta?.title || "页面");
const displayName = computed(() => currentUser.value?.displayName || "系统用户");
const seniorCareEnabled = computed(() => currentRole.value === "patient" && isSeniorCareModeEnabled());

async function handleLogout() {
  disableSeniorCareMode();
  await logout();
  await router.replace("/login");
}
</script>

<style scoped>
.medical-hub-shell {
  --hub-teal: #0f766e;
  --hub-teal-deep: #134e4a;
  --hub-teal-soft: rgba(15, 118, 110, 0.12);
  --hub-amber: #f59e0b;
  --hub-ink: #10231f;
  --hub-muted: #55726b;
  --hub-line: rgba(120, 150, 142, 0.24);
  --hub-surface: rgba(255, 255, 255, 0.72);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.18)),
    radial-gradient(circle at 8% 10%, rgba(45, 212, 191, 0.2), transparent 28%),
    radial-gradient(circle at 92% 12%, rgba(251, 191, 36, 0.22), transparent 24%),
    radial-gradient(circle at 52% 88%, rgba(14, 165, 233, 0.12), transparent 30%),
    #f4f7f2;
  font-family: "Segoe UI Variable Text", "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

.shell-ambient {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(16px);
  opacity: 0.9;
}

.ambient-one {
  top: 82px;
  left: -34px;
  width: 220px;
  height: 220px;
  background: rgba(45, 212, 191, 0.18);
}

.ambient-two {
  top: 120px;
  right: 54px;
  width: 180px;
  height: 180px;
  background: rgba(251, 191, 36, 0.18);
}

.ambient-three {
  bottom: 60px;
  right: 24%;
  width: 240px;
  height: 240px;
  background: rgba(56, 189, 248, 0.1);
}

.header {
  position: relative;
  z-index: 1;
  height: auto;
  min-height: 96px;
  margin: 18px 18px 0;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  color: #f7fffd;
  background:
    linear-gradient(125deg, rgba(19, 78, 74, 0.96) 0%, rgba(15, 118, 110, 0.92) 58%, rgba(245, 158, 11, 0.85) 100%);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(18px);
  animation: float-in 240ms ease-out;
}

.brand-shell {
  display: flex;
  align-items: center;
  gap: 18px;
}

.brand {
  max-width: 560px;
}

.brand h1 {
  margin: 0;
  font-size: clamp(24px, 2.5vw, 32px);
  line-height: 1.05;
  letter-spacing: 0.02em;
}

.subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
  opacity: 0.95;
}

.account-panel {
  display: flex;
  align-items: center;
  gap: 12px;
}

.account-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.account-name {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: 0.01em;
}

.logout-button {
  min-height: 44px;
  padding-inline: 18px;
  border-radius: 14px;
  border: none;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.16);
}

.shell-body {
  position: relative;
  z-index: 1;
  gap: 18px;
  padding: 18px;
}

.aside {
  background: transparent;
}

.aside-shell {
  min-height: calc(100vh - 150px);
  padding: 18px 16px;
  border: 1px solid var(--hub-line);
  border-radius: 28px;
  background: var(--hub-surface);
  backdrop-filter: blur(18px);
  box-shadow: 0 20px 42px rgba(15, 23, 42, 0.08);
}

.menu {
  border-right: 0;
  background: transparent;
}

:deep(.menu .el-menu-item) {
  min-height: 50px;
  margin-bottom: 6px;
  border-radius: 16px;
  font-weight: 600;
  transition: transform 180ms ease, background-color 180ms ease, box-shadow 180ms ease;
}

:deep(.menu .el-menu-item .el-icon) {
  font-size: 16px;
}

:deep(.menu .el-menu-item:hover) {
  transform: translateX(2px);
  background: rgba(15, 118, 110, 0.08);
}

:deep(.menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.14), rgba(245, 158, 11, 0.12));
  box-shadow: inset 0 0 0 1px rgba(15, 118, 110, 0.1);
}

.main {
  padding: 0;
}

.breadcrumb {
  margin-bottom: 14px;
  border: 1px solid var(--hub-line);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
}

.breadcrumb-shell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.breadcrumb-label {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--hub-teal);
}

.breadcrumb-pill {
  padding: 10px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: var(--hub-ink);
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.12), rgba(245, 158, 11, 0.16));
}

.router-view-shell {
  min-height: calc(100vh - 220px);
}

@keyframes float-in {
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
  .header,
  :deep(.menu .el-menu-item) {
    animation: none;
    transition: none;
  }
}

@media (max-width: 1100px) {
  .brand-shell {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 900px) {
  .header {
    margin: 14px 14px 0;
    padding: 18px;
    flex-direction: column;
    align-items: flex-start;
  }

  .account-panel {
    width: 100%;
    flex-wrap: wrap;
    justify-content: space-between;
  }

  .account-meta {
    align-items: flex-start;
  }

  .shell-body {
    padding: 14px;
    flex-direction: column;
  }

  .aside {
    width: 100% !important;
  }

  .aside-shell {
    min-height: auto;
  }

  .breadcrumb-shell {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
