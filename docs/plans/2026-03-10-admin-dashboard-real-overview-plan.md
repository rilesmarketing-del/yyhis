# 管理端首页真实总览 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为管理端首页提供真实的排班、预约、接诊、患者总览，并替换掉当前静态运营面板。

**Architecture:** 后端新增单一只读聚合接口 `/api/admin/dashboard/summary`，由 service 统一聚合排班、预约、接诊、患者数据并生成提醒。前端首页只请求这一份 summary，并通过独立 service 做页面映射，避免视图里堆叠统计逻辑。

**Tech Stack:** Spring Boot、Spring Data JPA、H2、Vue 3、Vite、Element Plus

---

### Task 1: 先写管理端 summary 后端红灯测试

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AdminDashboardIntegrationTest.java`

**Step 1: Write the failing test**

覆盖：
- admin 可访问 `/api/admin/dashboard/summary`
- summary 统计口径正确
- 非 admin 无法访问

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL，提示接口不存在或断言不匹配

**Step 3: Write minimal implementation**

只补足能够返回正确 summary 的最小 controller、service、dto 和 repository 查询。

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: admin dashboard 相关测试转绿

### Task 2: 实现管理端 summary 接口

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/AdminDashboardSummaryResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/AdminDashboardService.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/AdminDashboardController.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/DoctorScheduleRepository.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/AppointmentRecordRepository.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/VisitRecordRepository.java`

**Step 1: Write the failing test**

复用 Task 1 测试作为红灯保障。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL

**Step 3: Write minimal implementation**

- 补 repository 查询
- 聚合 summary
- 暴露 `/api/admin/dashboard/summary`

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: PASS

### Task 3: 先写前端映射红灯脚本

**Files:**
- Create: `frontend/src/services/adminDashboard.js`
- Create: `frontend/scripts/test-admin-dashboard.mjs`

**Step 1: Write the failing test**

覆盖：
- 统计卡映射
- 运营概览映射
- 管理提醒映射
- 快捷入口映射

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-dashboard.mjs`
Expected: FAIL，提示缺少映射函数

**Step 3: Write minimal implementation**

新增前端 summary 映射纯函数。

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-admin-dashboard.mjs`
Expected: PASS

### Task 4: 改造 AdminDashboard 页面

**Files:**
- Modify: `frontend/src/views/admin/AdminDashboard.vue`
- Modify: `frontend/src/services/adminSchedules.js`（仅在必要时统一 import 形式，不强制业务改动）

**Step 1: Write the failing test**

复用 Task 3 映射脚本作为最小行为保障。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-dashboard.mjs`
Expected: FAIL 或页面无法消费 summary 结构

**Step 3: Write minimal implementation**

- 页面请求 `/api/admin/dashboard/summary`
- 渲染真实统计卡、运营概览和管理提醒
- 保留快捷入口

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-admin-dashboard.mjs`
Expected: PASS

### Task 5: 整体验证

**Files:**
- Verify only

**Step 1: Run backend tests**

Run: `mvn test`
Expected: PASS

**Step 2: Run frontend build**

Run: `npm run build`
Expected: PASS

**Step 3: Run frontend bundle verification**

Run: `npm run verify:bundle`
Expected: PASS
