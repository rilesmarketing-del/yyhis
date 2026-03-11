<template>
  <div class="not-found">
    <el-result icon="warning" title="404" sub-title="页面不存在或路由无效">
      <template #extra>
        <el-button type="primary" @click="goHome">返回患者首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goHome() {
  router.push("/patient/dashboard");
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
