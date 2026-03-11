# Patient Appointments Light Persistence Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add demo patient switching on the frontend and persist appointment records to a local JSON file on the backend.

**Architecture:** Keep the current Vue page and Spring Boot API endpoints. Introduce a small frontend patient session helper backed by `localStorage`, and a backend file-backed repository that loads and saves appointment records while schedules remain seeded in memory.

**Tech Stack:** Vue 3, Vite, Element Plus, Spring Boot 2.7, Maven, JUnit 5, Jackson

---

### Task 1: Document the feature and identify code touch points

**Files:**
- Create: `docs/plans/2026-03-10-patient-appointments-light-persistence-design.md`
- Create: `docs/plans/2026-03-10-patient-appointments-light-persistence-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add failing backend persistence tests

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java`

**Step 1: Write a failing test for startup restore**

Test behavior:
- create a service backed by a temp file
- book an appointment
- create a fresh service using the same file
- verify the appointment still exists
- verify the schedule remaining slots stay reduced after reload

**Step 2: Write a failing test for cancelled records**

Test behavior:
- create and cancel an appointment
- create a fresh service using the same file
- verify the appointment is still present with `CANCELLED`
- verify the schedule remaining slots are fully restored

**Step 3: Run backend tests and confirm the new tests fail for the expected reason**

Run:
```powershell
mvn test
```

### Task 3: Implement backend file persistence

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java`
- Modify if needed: `backend/src/main/java/com/hospital/patientappointments/model/AppointmentRecord.java`
- Modify if needed: `backend/src/main/resources/application.properties`
- Create if needed: `backend/data/appointments.json`

**Step 1: Add a repository that loads and saves appointment records as JSON**

**Step 2: Refactor the service so it can be constructed with a repository for tests**

**Step 3: Load persisted appointments on startup and replay booked slot consumption**

**Step 4: Save to disk after booking and cancellation**

**Step 5: Run backend tests and confirm they pass**

Run:
```powershell
mvn test
```

### Task 4: Add frontend patient session helpers

**Files:**
- Create: `frontend/src/services/patientSession.js`
- Modify: `frontend/src/services/patientAppointments.js`

**Step 1: Add a small demo patient list and localStorage-backed active patient helpers**

**Step 2: Keep the appointment API wrapper aligned with the existing endpoints**

### Task 5: Update the patient appointment page

**Files:**
- Modify: `frontend/src/views/patient/PatientAppointments.vue`

**Step 1: Replace the hard-coded patient constant with the new session helper**

**Step 2: Add a patient selector UI and persist selection changes**

**Step 3: Refresh appointment data when the active patient changes**

**Step 4: Keep booking and cancellation requests scoped to the selected patient**

### Task 6: Verify the full lightweight flow

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

**Step 3: Briefly run the backend and confirm the schedules endpoint still responds**

```powershell
mvn spring-boot:run
```

Expected:
- API starts successfully when port 8080 is available
- persisted appointments reload from `backend/data/appointments.json`

**Step 4: Summarize any environment-only blockers separately from code issues**
