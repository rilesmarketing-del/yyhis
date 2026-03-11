# Patient Appointments Reschedule And Virtual Payment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add virtual payment and unpaid-only rescheduling to the patient appointment flow while preserving JSON-backed persistence.

**Architecture:** Extend the existing appointment record with a payment-status axis and keep all business rules inside `PatientAppointmentService`. Reuse the current JSON persistence layer, add two controller endpoints, and update the patient appointment page to expose pay/reschedule actions and statuses.

**Tech Stack:** Vue 3, Vite, Element Plus, Spring Boot 2.7, Maven, JUnit 5, Jackson

---

### Task 1: Save design and plan documents

**Files:**
- Create: `docs/plans/2026-03-10-patient-appointments-reschedule-payment-design.md`
- Create: `docs/plans/2026-03-10-patient-appointments-reschedule-payment-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add failing backend tests for payment and rescheduling

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java`
- Create if needed: `backend/src/main/java/com/hospital/patientappointments/model/PaymentStatus.java`

**Step 1: Write a failing test that new appointments default to `UNPAID`**

**Step 2: Write a failing test that virtual payment changes `UNPAID` to `PAID`**

**Step 3: Write a failing test that paid appointments cannot be rescheduled**

**Step 4: Write a failing test that unpaid appointments can be rescheduled and slots move correctly**

**Step 5: Write a failing test that cancelling a paid appointment yields `REFUNDED`**

**Step 6: Write a failing test that payment and reschedule states survive reload from JSON**

**Step 7: Run backend tests and confirm the new tests fail for the expected reason**

Run:
```powershell
mvn test
```

### Task 3: Implement backend model and service changes

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/model/PaymentStatus.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/RescheduleAppointmentRequest.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/model/AppointmentRecord.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/AppointmentRecordResponse.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java`
- Modify if needed: `backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java`

**Step 1: Add payment status to the persisted appointment model and response DTO**

**Step 2: Implement a `payAppointment` service method with strict state validation**

**Step 3: Implement a `rescheduleAppointment` service method that marks the original record `RESCHEDULED` and creates a new unpaid booking**

**Step 4: Update cancellation rules so paid cancellations become refunded**

**Step 5: Keep JSON load/save compatibility for older records with missing payment status**

**Step 6: Run backend tests and confirm they pass**

Run:
```powershell
mvn test
```

### Task 4: Expose payment and reschedule endpoints

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/controller/PatientAppointmentController.java`

**Step 1: Add `POST /{appointmentId}/pay`**

**Step 2: Add `POST /{appointmentId}/reschedule`**

**Step 3: Keep request/response shapes minimal and aligned with the service layer**

### Task 5: Update frontend appointment APIs and page interactions

**Files:**
- Modify: `frontend/src/services/patientAppointments.js`
- Modify: `frontend/src/views/patient/PatientAppointments.vue`

**Step 1: Add API wrappers for virtual payment and reschedule**

**Step 2: Show payment status and refine action visibility in the appointment table**

**Step 3: Add a reschedule dialog that lets the patient choose a new schedule**

**Step 4: Refresh schedule and appointment data after pay, cancel, and reschedule**

### Task 6: Verify backend, frontend, and runtime flow

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

**Step 3: Run a short backend smoke test on a non-conflicting port to verify pay/reschedule endpoints**

**Step 4: Summarize any environment-only blockers separately from code issues**