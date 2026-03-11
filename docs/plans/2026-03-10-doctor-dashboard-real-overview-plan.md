# 医生首页真实总览 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将医生首页从静态演示页升级为真实总览页，展示基于队列、接诊记录和患者列表的真实统计与提醒。

**Architecture:** 复用现有医生端接口，在前端新增纯映射服务聚合首页所需的统计卡、概览表和待处理事项，不新增后端聚合接口。首页视图只负责数据加载和展示，聚合规则沉淀到独立 service 方便脚本回归测试。

**Tech Stack:** Vue 3、Vite、Element Plus、现有 doctorClinic service

---

### Task 1: 先写医生首页映射红灯脚本

**Files:**
- Create: `frontend/scripts/test-doctor-dashboard.mjs`
- Create: `frontend/src/services/doctorDashboard.js`

**Step 1: Write the failing test**

覆盖：
- 统计卡计算
- 今日候诊与接诊概览合并
- 待处理事项生成

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-doctor-dashboard.mjs`
Expected: FAIL，提示缺少 `buildDoctorDashboardModel`

**Step 3: Write minimal implementation**

新增纯函数构建首页模型。

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-doctor-dashboard.mjs`
Expected: PASS

### Task 2: 改造 DoctorDashboard 页面

**Files:**
- Modify: `frontend/src/views/doctor/DoctorDashboard.vue`
- Modify: `frontend/src/services/doctorClinic.js`
- Modify: `frontend/src/services/auth.js`（如需读取当前医生显示名则复用现有能力，不强制变更）

**Step 1: Write the failing test**

复用 Task 1 的聚合脚本作为页面行为依赖的最小保障。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-doctor-dashboard.mjs`
Expected: FAIL 或现有页面无法消费聚合结果

**Step 3: Write minimal implementation**

- 首页加载 `fetchDoctorQueue`、`fetchDoctorRecords`、`fetchDoctorPatients`
- 调用 `buildDoctorDashboardModel`
- 渲染统计卡、概览表、待处理事项、快捷入口

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-doctor-dashboard.mjs`
Expected: PASS

### Task 3: 整体验证

**Files:**
- Verify only

**Step 1: Run frontend build**

Run: `npm run build`
Expected: PASS

**Step 2: Run frontend bundle verification**

Run: `npm run verify:bundle`
Expected: PASS
