<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span>药房库存管理</span>
        <el-tag type="warning">低库存 {{ lowStockCount }} 项</el-tag>
      </div>
    </template>
    <el-table :data="drugs" border>
      <el-table-column prop="code" label="药品编码" width="130" />
      <el-table-column prop="name" label="药品名称" min-width="180" />
      <el-table-column prop="spec" label="规格" width="140" />
      <el-table-column prop="stock" label="当前库存" width="110" />
      <el-table-column prop="safe" label="安全库存" width="110" />
      <el-table-column label="库存状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.stock <= row.safe ? 'danger' : 'success'">
            {{ row.stock <= row.safe ? "预警" : "正常" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expire" label="近效期" width="120" />
      <el-table-column label="操作" width="140">
        <template #default>
          <el-button link type="primary">入库</el-button>
          <el-button link>出库</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { computed } from "vue";

const drugs = [
  { code: "D1001", name: "阿莫西林胶囊", spec: "0.25g*24", stock: 46, safe: 40, expire: "2026-11" },
  { code: "D1008", name: "奥司他韦胶囊", spec: "75mg*10", stock: 18, safe: 30, expire: "2026-07" },
  { code: "D1012", name: "双氯芬酸钠缓释片", spec: "75mg*10", stock: 20, safe: 20, expire: "2026-06" },
];

const lowStockCount = computed(() => drugs.filter((d) => d.stock <= d.safe).length);
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
