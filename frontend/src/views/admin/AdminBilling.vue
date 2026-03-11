<template>
  <el-row :gutter="12">
    <el-col :xs="24" :lg="14">
      <el-card>
        <template #header>
          <span>收费项目配置</span>
        </template>
        <el-table :data="chargeItems" border>
          <el-table-column prop="code" label="项目编码" width="120" />
          <el-table-column prop="name" label="项目名称" min-width="170" />
          <el-table-column prop="price" label="单价(元)" width="100" />
          <el-table-column prop="insured" label="医保" width="90" />
          <el-table-column label="操作">
            <template #default>
              <el-button link type="primary">编辑</el-button>
              <el-button link>停用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :xs="24" :lg="10">
      <el-card>
        <template #header>
          <span>异常账单处理</span>
        </template>
        <el-table :data="exceptions" size="small" border>
          <el-table-column prop="billNo" label="账单号" width="120" />
          <el-table-column prop="reason" label="异常原因" min-width="150" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="90">
            <template #default>
              <el-button link type="primary">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
const chargeItems = [
  { code: "C1001", name: "普通门诊诊疗费", price: 35, insured: "是" },
  { code: "C1008", name: "胸部X线检查", price: 120, insured: "是" },
  { code: "C1012", name: "专家门诊挂号费", price: 60, insured: "否" },
];

const exceptions = [
  { billNo: "B2026030607", reason: "医保统筹计算失败", status: "待处理" },
  { billNo: "B2026030512", reason: "重复支付冲正", status: "处理中" },
];
</script>
