# 患者端真实报告与处方联动 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 让患者端报告页和处方页优先展示医生已完成接诊后录入的真实文本内容。

**Architecture:** 继续复用现有 `/api/patient/visits` 接口，把 `VisitRecord` 映射为真实报告与真实处方；报告页保留预约映射兜底，处方页移除静态假数据并改成真实文本列表。

**Tech Stack:** Vue 3、Vite、Element Plus、Node 脚本回归

---

### Task 1: 写失败的前端映射脚本

**Files:**
- Modify: `frontend/scripts/test-patient-reports.mjs`
- Create: `frontend/scripts/test-patient-prescriptions.mjs`

**Step 1: Write the failing test**

覆盖：
- 有已完成接诊记录时，报告优先使用 `reportNote`
- 没有真实报告时，回退预约映射报告
- 已完成接诊记录生成真实处方
- 无 `prescriptionNote` 时不生成处方

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: FAIL

Run: `node ./scripts/test-patient-prescriptions.mjs`
Expected: FAIL

**Step 3: Write minimal implementation**

实现最小映射逻辑让脚本通过。

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: PASS

Run: `node ./scripts/test-patient-prescriptions.mjs`
Expected: PASS

### Task 2: 实现真实报告与处方映射服务

**Files:**
- Modify: `frontend/src/services/patientReports.js`
- Create: `frontend/src/services/patientPrescriptions.js`

**Step 1: Write the failing test**

由 Task 1 脚本驱动。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: FAIL

**Step 3: Write minimal implementation**

- `patientReports.js` 支持 `visitRecords` 优先
- 新增 `patientPrescriptions.js` 做真实处方映射

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: PASS

Run: `node ./scripts/test-patient-prescriptions.mjs`
Expected: PASS

### Task 3: 接通患者报告页与处方页

**Files:**
- Modify: `frontend/src/views/patient/PatientReports.vue`
- Modify: `frontend/src/views/patient/PatientPrescriptions.vue`
- Reuse: `frontend/src/services/doctorClinic.js`

**Step 1: Write the failing test**

继续依赖脚本验证真实映射结果是否可被页面消费。

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: 若页面依赖未接通，仍可能失败或缺字段

**Step 3: Write minimal implementation**

- `PatientReports` 同时拉取真实接诊记录与预约数据
- `PatientPrescriptions` 改为读取真实接诊记录，移除静态假药品表

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: PASS

Run: `node ./scripts/test-patient-prescriptions.mjs`
Expected: PASS

### Task 4: 整体验证

**Files:**
- Verify only

**Step 1: Run report and prescription scripts**

Run: `node ./scripts/test-patient-reports.mjs`
Expected: PASS

Run: `node ./scripts/test-patient-prescriptions.mjs`
Expected: PASS

**Step 2: Run frontend build**

Run: `npm run build`
Expected: PASS

**Step 3: Run bundle verification**

Run: `npm run verify:bundle`
Expected: PASS