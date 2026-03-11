<template>
  <el-card class="page">
    <template #header>
      <div class="title-row">
        <div>
          <h2>{{ title }}</h2>
          <p>{{ modulePath }}</p>
        </div>
        <el-tag type="success">核心页面</el-tag>
      </div>
    </template>

    <el-alert
      title="当前为可运行的页面骨架，后续可在此接入真实接口与业务组件。"
      type="info"
      :closable="false"
      show-icon
      class="mb-14"
    />

    <el-descriptions :column="1" border>
      <el-descriptions-item label="角色">
        {{ roleLabel }}
      </el-descriptions-item>
      <el-descriptions-item label="页面名称">
        {{ title }}
      </el-descriptions-item>
      <el-descriptions-item label="页面路由">
        <code>{{ modulePath }}</code>
      </el-descriptions-item>
    </el-descriptions>

    <h3 class="section-title">子页面层级</h3>
    <div class="tags">
      <el-tag v-for="node in children" :key="node" class="tag" effect="plain">{{ node }}</el-tag>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { roleMeta } from "../../config/menu";

const route = useRoute();

const title = computed(() => route.meta?.title || "页面");
const modulePath = computed(() => route.meta?.modulePath || route.path);
const children = computed(() => route.meta?.children || []);
const roleLabel = computed(() => roleMeta[route.meta?.role]?.label || "未知");
</script>

<style scoped>
.page {
  border-radius: 14px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.title-row h2 {
  margin: 0;
  font-size: 20px;
}

.title-row p {
  margin: 6px 0 0;
  color: #5f706c;
  font-size: 13px;
}

.mb-14 {
  margin-bottom: 14px;
}

.section-title {
  margin: 16px 0 10px;
  font-size: 15px;
  color: #2f3b39;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  margin: 0;
}
</style>
