# 管理端组织与人员管理 + 患者自助注册 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 让管理端可以维护真实科室与人员账号，并让患者在登录页自助注册后立即进入统一登录体系。

**Architecture:** 后端新增 `Department` 与管理端组织接口，并扩展 `UserAccount` 承载基础人员字段；认证端新增患者注册接口。前端新增 `adminOrg` service 和真实 `AdminOrg` 页面，同时在登录页增加患者注册弹窗并接入注册接口。

**Tech Stack:** Spring Boot、Spring Data JPA、H2、Vue 3、Vite、Element Plus

---

### Task 1: 先写后端 AdminOrg 与注册红灯测试

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java`
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/AuthIntegrationTest.java`

**Step 1: Write the failing test**

覆盖：
- admin 可创建科室
- admin 可创建 doctor/admin/patient 账号
- 新账号可立即登录
- `/api/admin/org/summary` 返回真实科室与人员数据
- `/api/auth/register` 可创建 patient 且自动生成 `patientId`

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL，提示接口不存在、字段缺失或断言不匹配

**Step 3: Write minimal implementation**

补最小模型、dto、repository、service、controller。

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: AdminOrg 与注册相关测试转绿

### Task 2: 实现 Department 与统一账号扩展

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/model/Department.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/repository/DepartmentRepository.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/model/UserAccount.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/UserAccountRepository.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java`

**Step 1: Write the failing test**

复用 Task 1 红灯测试。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL

**Step 3: Write minimal implementation**

- 新增 `Department`
- 给 `UserAccount` 增加 `departmentId / title / mobile`
- 初始化演示科室与账号数据

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: 与模型相关错误消失

### Task 3: 实现管理端组织与人员接口

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/AdminOrgSummaryResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/CreateDepartmentRequest.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/CreateUserAccountRequest.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/AdminOrgController.java`

**Step 1: Write the failing test**

复用 Task 1 红灯测试。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL

**Step 3: Write minimal implementation**

- 返回科室树、人员列表、角色统计
- 支持新增科室
- 支持新增人员账号

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: AdminOrg 相关测试 PASS

### Task 4: 实现患者自助注册接口

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/RegisterPatientRequest.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/RegisterPatientResponse.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AuthService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/controller/AuthController.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/controller/AuthInterceptor.java`

**Step 1: Write the failing test**

复用 Task 1 红灯测试。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL

**Step 3: Write minimal implementation**

- 开放 `/api/auth/register`
- 自动生成 `patientId`
- 注册成功后返回账号与患者编号

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: 注册相关测试 PASS

### Task 5: 先写前端 AdminOrg 映射红灯脚本

**Files:**
- Create: `frontend/src/services/adminOrg.js`
- Create: `frontend/scripts/test-admin-org.mjs`

**Step 1: Write the failing test**

覆盖：
- 科室树映射
- 人员表映射
- 角色统计映射
- 科室选项扁平化映射

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-org.mjs`
Expected: FAIL，提示缺少映射函数

**Step 3: Write minimal implementation**

新增 `adminOrg` service 与纯映射函数。

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-admin-org.mjs`
Expected: PASS

### Task 6: 改造 AdminOrg 页面

**Files:**
- Modify: `frontend/src/views/admin/AdminOrg.vue`

**Step 1: Write the failing test**

复用 Task 5 红灯脚本。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-org.mjs`
Expected: FAIL 或页面无法消费真实接口结果

**Step 3: Write minimal implementation**

- 展示真实科室树与人员表
- 增加筛选、搜索
- 增加新增科室和新增人员弹窗

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-admin-org.mjs`
Expected: PASS

### Task 7: 改造登录页患者注册

**Files:**
- Modify: `frontend/src/services/auth.js`
- Modify: `frontend/src/views/common/LoginPage.vue`

**Step 1: Write the failing test**

以已有后端注册红灯与前端构建作为最小保障。

**Step 2: Run test to verify it fails**

Run: `npm run build`
Expected: FAIL 或缺少注册调用

**Step 3: Write minimal implementation**

- 新增 `registerPatient` 调用
- 登录页增加患者注册弹窗
- 注册成功后回填登录表单

**Step 4: Run test to verify it passes**

Run: `npm run build`
Expected: PASS

### Task 8: 整体验证

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