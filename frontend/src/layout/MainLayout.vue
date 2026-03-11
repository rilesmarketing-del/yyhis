<template>
  <el-container class="shell">
    <el-header class="header">
      <div class="brand">
        <p class="eyebrow">Hospital Demo</p>
        <h1>智慧医院业务系统</h1>
        <p class="subtitle">Spring Boot + Vue 3 + Element Plus 演示工作台</p>
      </div>

      <div class="account-panel">
        <div class="account-meta">
          <span class="account-name">{{ displayName }}</span>
          <span class="account-role">{{ roleLabel }} · {{ currentUser?.username || "-" }}</span>
        </div>
        <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="250px" class="aside">
        <el-menu
          :default-active="activePath"
          class="menu"
          router
          background-color="#fcfdfc"
          text-color="#2f3b39"
          active-text-color="#0f766e"
        >
          <el-menu-item v-for="item in currentMenus" :key="item.path" :index="item.path">
            <el-icon v-if="item.icon">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <el-card class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ roleLabel }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </el-card>

        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { roleMenus, roleMeta } from "../config/menu";
import { authState, logout } from "../services/auth";

const route = useRoute();
const router = useRouter();

const currentRole = computed(() => route.meta?.role || route.path.split("/")[1] || authState.currentUser?.role || "patient");
const currentUser = computed(() => authState.currentUser);
const currentMenus = computed(() => roleMenus[currentRole.value] || []);
const activePath = computed(() => route.path);
const roleLabel = computed(() => roleMeta[currentRole.value]?.label || "未知角色");
const currentTitle = computed(() => route.meta?.title || "页面");
const displayName = computed(() => currentUser.value?.displayName || "演示账号");

async function handleLogout() {
  await logout();
  await router.replace("/login");
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
  background: radial-gradient(circle at 7% 8%, #d7f2eb 0%, transparent 32%), radial-gradient(circle at 92% 15%, #fff2c7 0%, transparent 28%), #f3f7f4;
}

.header {
  height: auto;
  min-height: 82px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: linear-gradient(120deg, #134e4a, #0f766e);
  color: #f5fffd;
  border-bottom: 1px solid rgba(255, 255, 255, 0.25);
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  opacity: 0.85;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  opacity: 0.92;
}

.account-panel {
  display: flex;
  align-items: center;
  gap: 14px;
}

.account-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.account-name {
  font-size: 15px;
  font-weight: 600;
}

.account-role {
  font-size: 12px;
  opacity: 0.88;
}

.aside {
  border-right: 1px solid #dde5e1;
  background: #fcfdfc;
}

.menu {
  border-right: 0;
  min-height: calc(100vh - 82px);
}

.main {
  padding: 16px;
}

.breadcrumb {
  margin-bottom: 14px;
}

@media (max-width: 900px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .account-panel {
    width: 100%;
    justify-content: space-between;
  }

  .account-meta {
    align-items: flex-start;
  }

  .aside {
    width: 100% !important;
    border-right: 0;
    border-bottom: 1px solid #dde5e1;
  }
}
</style>