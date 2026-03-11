# Patient Payments Appointment Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the static patient payments page with a real view over appointment payment data and keep virtual payment working from both pages.

**Architecture:** Reuse appointment records as the sole source of payment truth. Add small backend response fields needed for display (`fee`, `paidAt`), then update the payments page to derive pending and history tabs from the current patient's appointment list and call the existing payment endpoint.

**Tech Stack:** Vue 3, Vite, Element Plus, Spring Boot 2.7, Maven, JUnit 5, Jackson

---

### Task 1: Save design and plan documents

**Files:**
- Create: `docs/plans/2026-03-10-patient-payments-appointment-integration-design.md`
- Create: `docs/plans/2026-03-10-patient-payments-appointment-integration-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add failing backend tests for payment display fields

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java`

**Step 1: Write a failing test that paid appointments expose `fee` and `paidAt`**

**Step 2: Write a failing test that refunded appointments keep `fee` and `paidAt` after reload**

**Step 3: Run backend tests and confirm the new tests fail for the expected missing fields**

Run:
```powershell
mvn test
```

### Task 3: Implement backend support for payment display fields

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/model/AppointmentRecord.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/AppointmentRecordResponse.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java`

**Step 1: Persist `fee` on appointment creation**

**Step 2: Persist `paidAt` when virtual payment succeeds**

**Step 3: Include both fields in appointment responses**

**Step 4: Run backend tests and confirm they pass**

Run:
```powershell
mvn test
```

### Task 4: Update frontend payment services and page

**Files:**
- Modify: `frontend/src/services/patientAppointments.js`
- Modify: `frontend/src/views/patient/PatientPayments.vue`
- Reuse: `frontend/src/services/patientSession.js`

**Step 1: Reuse the current patient selection from session storage**

**Step 2: Load real appointments and map them into pending/history payment rows**

**Step 3: Call the existing virtual payment API from the payments page**

**Step 4: Switch to history after successful payment and refresh data**

### Task 5: Verify end-to-end behavior

**Files:**
- Modify if needed: `backend/*`
- Modify if needed: `frontend/*`

**Step 1: Run backend verification**

```powershell
mvn test
```

**Step 2: Run frontend verification**

```powershell
npm run build
npm run verify:bundle
```

**Step 3: Run a short backend smoke test to confirm paid appointments expose `fee` and `paidAt`**

**Step 4: Summarize any environment-only blockers separately from code issues**