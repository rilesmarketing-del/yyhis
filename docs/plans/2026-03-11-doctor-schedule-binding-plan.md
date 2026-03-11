# Doctor Schedule Binding Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Bind schedules and doctor-side workflows to stable doctor accounts, and replace the static doctor schedule page with a real read-only schedule view.

**Architecture:** Extend `DoctorSchedule` and `AppointmentRecord` with `doctorUsername`, teach admin scheduling to select real doctor accounts, and query doctor queue and schedule by `doctorUsername` instead of display-name text matching. Keep doctor-side schedule read-only and management-controlled.

**Tech Stack:** Spring Boot, Spring Data JPA, H2, Vue 3, Vite, Element Plus

---

### Task 1: Add backend failing tests for stable doctor binding

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/AdminScheduleIntegrationTest.java`
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/DoctorClinicIntegrationTest.java`
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/DoctorScheduleIntegrationTest.java`

**Step 1: Write the failing tests**

- Assert admin schedule creation accepts a real doctor account identity and persists `doctorUsername`
- Assert paid appointments preserve `doctorUsername`
- Assert doctor queue lookup works via account binding
- Assert `/api/doctor/schedule/mine` only returns the current doctor's schedules

**Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: failures for missing `doctorUsername` handling and missing doctor schedule endpoint

### Task 2: Add frontend failing tests for admin and doctor schedule mapping

**Files:**
- Create: `frontend/scripts/test-doctor-schedule.mjs`
- Modify: `frontend/scripts/test-admin-scheduling.mjs` if needed

**Step 1: Write the failing tests**

- Assert admin-side doctor options can map selected doctor accounts into schedule form state
- Assert doctor-side schedule model formats a real schedule list for display

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: fail because mapping helper or service does not exist yet

### Task 3: Implement backend doctor-account schedule binding

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/model/DoctorSchedule.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/model/AppointmentRecord.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/AdminScheduleRequest.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/AdminScheduleResponse.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/DoctorScheduleResponse.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/DoctorScheduleRepository.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AdminScheduleService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/DoctorClinicService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java`
- Modify: related DTOs or repositories as required

**Step 1: Write minimal implementation**

- Add `doctorUsername` fields
- Persist `doctorUsername` on schedule create and update
- Persist `doctorUsername` on appointment create
- Replace doctor queue and ownership checks to use `doctorUsername`
- Add compatibility backfill for legacy records if needed

**Step 2: Run test to verify it passes**

Run: `mvn test`
Expected: backend tests pass

### Task 4: Implement doctor schedule backend endpoint

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/DoctorOwnScheduleResponse.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/DoctorScheduleService.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/DoctorScheduleController.java`

**Step 1: Write minimal implementation**

- Add `GET /api/doctor/schedule/mine`
- Return only schedules owned by the current doctor
- Sort by date and time slot

**Step 2: Run test to verify it passes**

Run: `mvn test`
Expected: new doctor schedule integration coverage passes

### Task 5: Implement admin schedule doctor selection and doctor-side real page

**Files:**
- Modify: `frontend/src/views/admin/AdminScheduling.vue`
- Modify: `frontend/src/services/adminSchedules.js`
- Create: `frontend/src/services/doctorSchedule.js`
- Modify: `frontend/src/views/doctor/DoctorSchedule.vue`

**Step 1: Write minimal implementation**

- Load real doctor options for admin scheduling
- Replace free-text doctor fields with doctor selection and auto-filled metadata
- Replace static doctor schedule page with real read-only table and refresh action

**Step 2: Run test to verify it passes**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: mapping tests pass

### Task 6: Final verification

**Files:**
- Verify all modified backend and frontend files

**Step 1: Run backend verification**

Run: `mvn test`
Expected: all backend tests green

**Step 2: Run frontend verification**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: pass

**Step 3: Run production build verification**

Run: `npm run build`
Expected: build success

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: all bundles within configured threshold
