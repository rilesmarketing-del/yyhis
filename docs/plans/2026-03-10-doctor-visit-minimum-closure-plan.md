# 医生端接诊最小闭环 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 建立医生端最小接诊闭环，让已支付预约进入医生待接诊队列并生成真实就诊记录。

**Architecture:** 后端新增 `VisitRecord` 实体与医生端查询/编辑接口，继续复用现有 Spring Boot、H2、JPA 与最小 Bearer 鉴权；前端将医生端页面从静态占位切换为真实数据视图，并让患者端就诊记录优先读取真实就诊记录。

**Tech Stack:** Spring Boot 2.7、Spring Data JPA、H2、Vue 3、Vite、Element Plus

---

### Task 1: 写医生端后端失败测试

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/DoctorClinicIntegrationTest.java`
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/DoctorRecordAuthorizationIntegrationTest.java`

**Step 1: Write the failing test**

覆盖：
- 医生登录后只能看到自己名下已支付预约
- 开始接诊成功创建记录
- 非医生角色无法访问医生接口
- 非本人医生不能读取或完成他人的记录

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL，提示医生端接口不存在或行为不符合预期

**Step 3: Write minimal implementation**

先只补足能让接口返回正确结构的最小实现。

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: 相关医生端测试转绿

### Task 2: 新增就诊记录后端模型与服务

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/model/VisitStatus.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/model/VisitRecord.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/repository/VisitRecordRepository.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/DoctorQueueItemResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/VisitRecordRequest.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/VisitRecordResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/DoctorPatientResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/DoctorClinicService.java`

**Step 1: Write the failing test**

用集成测试驱动 `VisitRecord` 的创建、查询、保存、完成逻辑。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL，提示缺少 `VisitRecord` 相关实现

**Step 3: Write minimal implementation**

- 增加实体、仓储、DTO、服务
- 复用预约表查询已支付预约作为待接诊队列
- 使用登录医生身份过滤数据

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: 医生端服务逻辑通过

### Task 3: 新增医生端控制器与接口鉴权联动

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/DoctorClinicController.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/controller/AuthInterceptor.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/controller/ApiExceptionHandler.java`

**Step 1: Write the failing test**

通过 `MockMvc` 集成测试验证医生接口返回、未授权拒绝、跨医生访问拒绝。

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: FAIL，提示控制器缺失或鉴权错误

**Step 3: Write minimal implementation**

实现：
- `GET /api/doctor/clinic/queue`
- `POST /api/doctor/clinic/{appointmentId}/start`
- `GET /api/doctor/records`
- `GET /api/doctor/records/{visitId}`
- `PUT /api/doctor/records/{visitId}`
- `POST /api/doctor/records/{visitId}/complete`
- `GET /api/doctor/patients`

**Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: 医生接口测试通过

### Task 4: 患者端就诊记录优先读取真实就诊记录

**Files:**
- Modify: `frontend/src/views/patient/PatientVisits.vue`
- Modify: `frontend/src/services/patientVisits.js`
- Create: `frontend/src/services/doctorClinic.js`

**Step 1: Write the failing test**

新增一个小型前端脚本，验证有真实 `VisitRecord` 时优先展示真实记录。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-patient-visits.mjs`
Expected: FAIL 或断言不匹配

**Step 3: Write minimal implementation**

- 增加读取真实就诊记录的数据服务
- 优先展示真实记录，必要时保留旧预约映射兜底

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-patient-visits.mjs`
Expected: PASS

### Task 5: 接通医生端接诊页、病历页、患者页

**Files:**
- Modify: `frontend/src/views/doctor/DoctorClinic.vue`
- Modify: `frontend/src/views/doctor/DoctorRecords.vue`
- Modify: `frontend/src/views/doctor/DoctorPatients.vue`
- Create: `frontend/scripts/test-doctor-visit-flow.mjs`

**Step 1: Write the failing test**

新增脚本覆盖：
- 待接诊队列映射
- 就诊记录列表映射
- 医生患者列表聚合

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-doctor-visit-flow.mjs`
Expected: FAIL

**Step 3: Write minimal implementation**

- `DoctorClinic` 显示真实待接诊列表并支持开始接诊
- `DoctorRecords` 显示和编辑真实病历字段
- `DoctorPatients` 显示真实患者列表与详情摘要

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-doctor-visit-flow.mjs`
Expected: PASS

### Task 6: 整体验证

**Files:**
- Verify only

**Step 1: Run backend tests**

Run: `mvn test`
Expected: 全部通过

**Step 2: Run frontend build**

Run: `npm run build`
Expected: PASS

**Step 3: Run frontend bundle verification**

Run: `npm run verify:bundle`
Expected: PASS

**Step 4: Manual smoke summary**

检查登录医生账号后，队列页、病历页、患者页是否具备可演示入口与真实数据联动。