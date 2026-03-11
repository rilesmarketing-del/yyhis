<template>
  <el-tabs v-model="tab">
    <el-tab-pane label="参数配置" name="params">
      <el-card>
        <el-form :model="params" label-width="130px">
          <el-form-item label="会话超时(分钟)">
            <el-input-number v-model="params.sessionTimeout" :min="10" :max="240" />
          </el-form-item>
          <el-form-item label="预约提前天数">
            <el-input-number v-model="params.appointDays" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="停诊短信模板">
            <el-input v-model="params.cancelTemplate" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary">保存参数</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="字典管理" name="dict">
      <el-card>
        <el-table :data="dicts" border>
          <el-table-column prop="type" label="字典类型" width="180" />
          <el-table-column prop="key" label="键" width="140" />
          <el-table-column prop="value" label="值" min-width="180" />
          <el-table-column label="操作" width="120">
            <template #default>
              <el-button link type="primary">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="操作日志" name="ops">
      <el-card>
        <el-table :data="logs" border>
          <el-table-column prop="time" label="时间" width="170" />
          <el-table-column prop="user" label="用户" width="120" />
          <el-table-column prop="action" label="操作" min-width="220" />
          <el-table-column prop="ip" label="IP" width="130" />
        </el-table>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="审计日志" name="audit">
      <el-card>
        <el-table :data="audits" border>
          <el-table-column prop="time" label="时间" width="170" />
          <el-table-column prop="module" label="模块" width="140" />
          <el-table-column prop="event" label="审计事件" min-width="220" />
          <el-table-column prop="result" label="结果" width="100" />
        </el-table>
      </el-card>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { ref } from "vue";

const tab = ref("params");

const params = ref({
  sessionTimeout: 60,
  appointDays: 7,
  cancelTemplate: "您预约的门诊已停诊，请及时改约。",
});

const dicts = [
  { type: "科室", key: "RESP", value: "呼吸内科" },
  { type: "支付方式", key: "WX", value: "微信支付" },
  { type: "号源类型", key: "EXP", value: "专家号" },
];

const logs = [
  { time: "2026-03-06 09:50", user: "admin", action: "修改预约提前天数为 7", ip: "10.10.2.15" },
  { time: "2026-03-06 10:05", user: "ops01", action: "导出 3 月业务报表", ip: "10.10.2.33" },
];

const audits = [
  { time: "2026-03-06 09:55", module: "权限中心", event: "角色权限变更", result: "成功" },
  { time: "2026-03-06 10:12", module: "收费医保", event: "异常账单人工冲正", result: "成功" },
];
</script>
