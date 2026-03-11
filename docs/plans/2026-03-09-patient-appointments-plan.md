# Patient Appointments Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Restructure the project into `frontend/` and `backend/`, then ship a patient appointment booking MVP backed by a Spring Boot API.

**Architecture:** Keep the existing Vue application as an independent frontend under `frontend/`. Add a separate Spring Boot backend under `backend/` with in-memory schedule and appointment state. Connect the patient appointment page to the backend through a small API client layer.

**Tech Stack:** Vue 3, Vite, Element Plus, Java 15, Spring Boot, Maven, JUnit 5

---

### Task 1: Create project documentation and target directories

**Files:**
- Create: `docs/plans/2026-03-09-patient-appointments-design.md`
- Create: `docs/plans/2026-03-09-patient-appointments-plan.md`
- Create: `frontend/`
- Create: `backend/`

**Step 1: Create the target directory structure**

Run:
```powershell
New-Item -ItemType Directory -Force frontend, backend
```

**Step 2: Verify the directories exist**

Run:
```powershell
Get-ChildItem
```
Expected: `frontend` and `backend` are present.

### Task 2: Move the existing frontend project under `frontend`

**Files:**
- Move: `src` -> `frontend/src`
- Move: `scripts` -> `frontend/scripts`
- Move: `index.html` -> `frontend/index.html`
- Move: `package.json` -> `frontend/package.json`
- Move: `package-lock.json` -> `frontend/package-lock.json`
- Move: `vite.config.js` -> `frontend/vite.config.js`
- Move: `node_modules` -> `frontend/node_modules`
- Move: `hospital_pc_ia_page.html` -> `frontend/hospital_pc_ia_page.html`
- Skip or delete build output: `dist`

**Step 1: Move one frontend file or folder at a time**

**Step 2: Run a frontend build from `frontend/`**

Run:
```powershell
npm run build
```
Expected: Vite build succeeds.

**Step 3: Run bundle verification**

Run:
```powershell
npm run verify:bundle
```
Expected: all JavaScript bundles are within the configured limit.

### Task 3: Add a failing backend test for appointment rules

**Files:**
- Create: `backend/src/test/java/com/hospital/appointments/service/PatientAppointmentServiceTest.java`
- Create: `backend/pom.xml`

**Step 1: Write the failing test**

Test behaviors:
- books successfully when slots remain
- rejects duplicate booking
- rejects booking when slots are exhausted
- cancels booked appointments and restores one slot

**Step 2: Run test to verify it fails**

Run:
```powershell
mvn test
```
Expected: fail because service classes do not exist yet.

### Task 4: Implement backend models and service

**Files:**
- Create: `backend/src/main/java/com/hospital/appointments/model/DoctorSchedule.java`
- Create: `backend/src/main/java/com/hospital/appointments/model/AppointmentRecord.java`
- Create: `backend/src/main/java/com/hospital/appointments/model/AppointmentStatus.java`
- Create: `backend/src/main/java/com/hospital/appointments/dto/*.java`
- Create: `backend/src/main/java/com/hospital/appointments/service/PatientAppointmentService.java`

**Step 1: Write minimal implementation for the service to satisfy tests**

**Step 2: Re-run tests**

Run:
```powershell
mvn test
```
Expected: tests pass.

### Task 5: Add Spring Boot application and controller

**Files:**
- Create: `backend/src/main/java/com/hospital/appointments/PatientAppointmentsApplication.java`
- Create: `backend/src/main/java/com/hospital/appointments/controller/PatientAppointmentController.java`
- Create: `backend/src/main/resources/application.properties`

**Step 1: Add controller endpoints**
- `GET /api/patient/appointments/schedules`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments/my`
- `POST /api/patient/appointments/{id}/cancel`

**Step 2: Run application**

Run:
```powershell
mvn spring-boot:run
```
Expected: backend starts locally.

### Task 6: Add a frontend API client

**Files:**
- Create: `frontend/src/services/patientAppointments.js`

**Step 1: Add API wrappers**
- `fetchSchedules`
- `createAppointment`
- `fetchMyAppointments`
- `cancelAppointment`

**Step 2: Keep the client minimal and aligned to backend DTOs**

### Task 7: Refactor the patient appointment page to use real API data

**Files:**
- Modify: `frontend/src/views/patient/PatientAppointments.vue`
- Create if needed: `frontend/src/components/patient/*.vue`

**Step 1: Write the failing UI expectation mentally against current behavior**
Current page is static and cannot query backend schedules or show live appointment records.

**Step 2: Replace static data with reactive API-driven state**
- filter form
- schedule list
- booking action
- my appointments list
- cancellation action
- loading, empty, and error states

**Step 3: Run frontend build**

Run:
```powershell
npm run build
```
Expected: build succeeds.

### Task 8: Verify the full flow and document blocked checks

**Files:**
- Modify if needed: `frontend/src/views/patient/PatientAppointments.vue`
- Modify if needed: `backend/*`

**Step 1: Run frontend verification**

Run:
```powershell
npm run build
npm run verify:bundle
```

**Step 2: Run backend verification if Maven is available**

Run:
```powershell
mvn test
mvn spring-boot:run
```

**Step 3: If Maven is unavailable, report the exact blocked command and why**

**Step 4: Summarize the final project structure and the patient appointment flow**