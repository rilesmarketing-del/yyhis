# P0 Database Auth Scheduling Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Move the Spring Boot app from JSON-backed demo persistence to H2-backed persistence with minimal auth and real admin-managed schedules.

**Architecture:** Keep Spring Boot, add H2 + JPA entities and repositories, add a lightweight token auth layer, preserve patient appointment API paths, and wire the frontend to a backend-driven login plus real admin schedule CRUD.

**Tech Stack:** Spring Boot 2.7, H2, Spring Data JPA, Vue 3, Vite, Element Plus

---

### Task 1: Save design and plan documents

**Files:**
- Create: `docs/plans/2026-03-10-p0-database-auth-scheduling-design.md`
- Create: `docs/plans/2026-03-10-p0-database-auth-scheduling-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add failing backend auth and scheduling tests

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AuthIntegrationTest.java`
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AdminScheduleIntegrationTest.java`
- Modify if needed: `backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java`

**Step 1: Write failing tests for login success and unauthenticated rejection**

**Step 2: Write failing tests for admin schedule create/update/disable**

**Step 3: Run backend tests and confirm they fail for the expected missing behavior**

Run:
```powershell
mvn test
```

### Task 3: Implement H2 persistence and token auth in Spring Boot

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`
- Create: JPA entities and repositories under `backend/src/main/java/com/hospital/patientappointments/**`
- Create: auth DTOs, controller, service, and interceptor/filter
- Refactor: `backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java`
- Refactor: `backend/src/main/java/com/hospital/patientappointments/controller/PatientAppointmentController.java`
- Create: admin schedule controller/service as needed

**Step 1: Add H2 and JPA dependencies**

**Step 2: Add demo account and schedule seed initialization**

**Step 3: Replace JSON-backed appointment persistence with JPA-backed persistence**

**Step 4: Add login, me, logout APIs and bearer-token enforcement**

**Step 5: Add admin schedule CRUD and wire patient schedule query to database schedules**

**Step 6: Re-run backend tests until green**

Run:
```powershell
mvn test
```

### Task 4: Add frontend auth service and protected routing

**Files:**
- Create: `frontend/src/services/auth.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/layout/MainLayout.vue`
- Create: `frontend/src/views/common/LoginPage.vue`
- Modify if needed: `frontend/src/main.js`

**Step 1: Add login state storage and token helpers**

**Step 2: Add login page and route guard behavior**

**Step 3: Replace free role switching with current-user display and logout**

### Task 5: Wire patient and admin API clients to token auth

**Files:**
- Modify: `frontend/src/services/patientAppointments.js`
- Create if needed: `frontend/src/services/adminSchedules.js`
- Modify: `frontend/src/views/admin/AdminScheduling.vue`
- Modify if needed: patient views that rely on appointment APIs

**Step 1: Make API requests send bearer token**

**Step 2: Replace static admin scheduling view with real CRUD form/table actions**

**Step 3: Keep patient scheduling, payment, reschedule, and cancel flows compatible with backend auth**

### Task 6: Verify P0 slice

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

**Step 3: Summarize any remaining P0 gaps separately from completed behavior**