# 管理端账号与角色查看页 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将管理端权限页升级成真实的账号与角色查看页，展示系统内真实演示账号和角色概览。

**Architecture:** 后端新增一个只读聚合接口 /api/admin/auth/accounts，从 UserAccount 生成账号列表与角色汇总；前端新增独立 service 做页面模型映射，并将 AdminAuth.vue 改造成“账号表 + 角色概览”的真实只读页面。

**Tech Stack:** Spring Boot、Spring Data JPA、H2、Vue 3、Vite、Element Plus

---

### Task 1: 先写 AdminAuth 后端红灯测试

**Files:**
- Create: ackend/src/test/java/com/hospital/patientappointments/integration/AdminAuthIntegrationTest.java

**Step 1: Write the failing test**

覆盖：
- admin 可访问 GET /api/admin/auth/accounts
- doctor 访问被拒绝
- 返回 ccounts 与 oleSummary
- 演示账号 patient / doctor / admin 出现在结果中

**Step 2: Run test to verify it fails**

Run: mvn test
Expected: FAIL，提示接口不存在或断言不匹配

**Step 3: Write minimal implementation**

补最小 dto、service、controller。

**Step 4: Run test to verify it passes**

Run: mvn test
Expected: AdminAuth 相关测试转绿

### Task 2: 实现 admin 账号只读接口

**Files:**
- Create: ackend/src/main/java/com/hospital/patientappointments/dto/AdminAuthAccountsResponse.java
- Create: ackend/src/main/java/com/hospital/patientappointments/service/AdminAuthService.java
- Create: ackend/src/main/java/com/hospital/patientappointments/controller/AdminAuthController.java

**Step 1: Write the failing test**

复用 Task 1 测试作为红灯保障。

**Step 2: Run test to verify it fails**

Run: mvn test
Expected: FAIL

**Step 3: Write minimal implementation**

- 读取真实 UserAccount
- 生成账号列表
- 生成角色汇总与说明

**Step 4: Run test to verify it passes**

Run: mvn test
Expected: PASS

### Task 3: 先写前端映射红灯脚本

**Files:**
- Create: rontend/src/services/adminAuth.js
- Create: rontend/scripts/test-admin-auth.mjs

**Step 1: Write the failing test**

覆盖：
- ccounts 映射为账号表
- oleSummary 映射为右侧概览
- 根据选中账号返回正确角色说明

**Step 2: Run test to verify it fails**

Run: 
ode ./scripts/test-admin-auth.mjs
Expected: FAIL，提示缺少映射函数

**Step 3: Write minimal implementation**

新增 AdminAuth 页面模型映射 service。

**Step 4: Run test to verify it passes**

Run: 
ode ./scripts/test-admin-auth.mjs
Expected: PASS

### Task 4: 改造 AdminAuth 页面

**Files:**
- Modify: rontend/src/views/admin/AdminAuth.vue

**Step 1: Write the failing test**

复用 Task 3 映射脚本作为最小保障。

**Step 2: Run test to verify it fails**

Run: 
ode ./scripts/test-admin-auth.mjs
Expected: FAIL 或页面仍无法消费真实接口结果

**Step 3: Write minimal implementation**

- 页面请求 /api/admin/auth/accounts
- 左侧展示真实账号表
- 右侧展示角色概览与当前角色说明
- 移除静态权限树和伪操作按钮

**Step 4: Run test to verify it passes**

Run: 
ode ./scripts/test-admin-auth.mjs
Expected: PASS

### Task 5: 整体验证

**Files:**
- Verify only

**Step 1: Run backend tests**

Run: mvn test
Expected: PASS

**Step 2: Run frontend build**

Run: 
pm run build
Expected: PASS

**Step 3: Run frontend bundle verification**

Run: 
pm run verify:bundle
Expected: PASS