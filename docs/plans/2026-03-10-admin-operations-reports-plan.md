# 管理端运营报表 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将管理端报表页升级为真实运营报表页，展示真实排班、预约、接诊、患者统计。

**Architecture:** 后端新增一个最小只读接口 `/api/admin/reports/operations`，聚合当前真实运营数据；前端新增独立映射 service，将返回值转换为卡片和统计表视图模型。页面保持单页、无导出、无筛选。

**Tech Stack:** Spring Boot、Spring Data JPA、H2、Vue 3、Vite、Element Plus

---

### Task 1: 先写运营报表后端红灯测试

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AdminOperationsReportIntegrationTest.java`

**Step 1: Write the failing test**

覆盖：
- admin 可访问 `/api/admin/reports/operations`
- cards 和 table 统计正确
- 非 admin 被拒绝

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL，提示接口不存在或断言不匹配

**Step 3: Write minimal implementation**

补最小 dto、service、controller。

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: 运营报表相关测试转绿

### Task 2: 实现运营报表接口

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/AdminOperationsReportResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/AdminOperationsReportService.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/AdminReportsController.java`

**Step 1: Write the failing test**

复用 Task 1 测试作为红灯保障。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL

**Step 3: Write minimal implementation**

聚合 cards 与 table 返回。

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: PASS

### Task 3: 先写前端映射红灯脚本

**Files:**
- Create: `frontend/src/services/adminReports.js`
- Create: `frontend/scripts/test-admin-reports.mjs`

**Step 1: Write the failing test**

覆盖：
- 卡片映射
- 统计表映射

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-reports.mjs`
Expected: FAIL，提示缺少映射函数

**Step 3: Write minimal implementation**

新增运营报表映射 service。

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-admin-reports.mjs`
Expected: PASS

### Task 4: 改造 AdminReports 页面

**Files:**
- Modify: `frontend/src/views/admin/AdminReports.vue`

**Step 1: Write the failing test**

复用 Task 3 映射脚本作为最小保障。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-reports.mjs`
Expected: FAIL 或页面无法消费接口结构

**Step 3: Write minimal implementation**

- 页面请求 `/api/admin/reports/operations`
- 渲染真实卡片与统计表
- 移除导出历史与静态报表卡片

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-admin-reports.mjs`
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