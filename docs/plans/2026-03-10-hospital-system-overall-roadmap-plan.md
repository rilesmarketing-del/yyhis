# Hospital System Overall Roadmap Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Provide a phase-by-phase roadmap for turning the current patient-side demo into a fuller hospital information system.

**Architecture:** Use the existing frontend and backend split as the base, then sequence work across foundations, patient-side business, doctor workflow, admin operations, and engineering enablement. Prioritize removing demo-only constraints before broadening feature depth.

**Tech Stack:** Vue 3, Vite, Element Plus, Spring Boot, Maven, local JSON persistence today, future database-backed persistence

---

### Task 1: Foundation Hardening (P0)

**Files:**
- Modify: `backend/*`
- Modify: `frontend/*`
- Create: authentication and persistence documents as needed

**Step 1: Replace lightweight JSON persistence with database-backed persistence**

**Step 2: Add a minimal authentication and role model**

**Step 3: Normalize backend validation, DTOs, and error responses**

**Step 4: Add reproducible local startup and environment configuration**

### Task 2: Scheduling and Booking Source of Truth (P0)

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/**`
- Modify: `frontend/src/views/admin/**`
- Modify: `frontend/src/views/patient/PatientAppointments.vue`

**Step 1: Move schedules from seed data to persistent admin-managed data**

**Step 2: Make admin scheduling pages drive real slot availability**

**Step 3: Keep patient booking, cancel, pay, and reschedule aligned to real slot data**

### Task 3: Patient-Side Business Completion (P1)

**Files:**
- Modify: `frontend/src/views/patient/**`
- Modify: `backend/**`

**Step 1: Turn payment center into real bill and invoice views**

**Step 2: Replace derived visits and reports with backend-managed records when doctor-side data exists**

**Step 3: Link prescriptions and medication pickup into the patient experience**

**Step 4: Add reminders and stronger patient-facing status messaging**

### Task 4: Doctor Workflow Closure (P1)

**Files:**
- Modify: `frontend/src/views/doctor/**`
- Modify: `backend/**`

**Step 1: Make doctor dashboard and clinic queue data-driven**

**Step 2: Add real patient visit handling and medical record workflows**

**Step 3: Let doctor actions create patient-visible reports, prescriptions, and clinical history**

### Task 5: Admin Operating Controls (P1)

**Files:**
- Modify: `frontend/src/views/admin/**`
- Modify: `backend/**`

**Step 1: Add real department, staff, and organization management**

**Step 2: Add billing catalog and reimbursement administration**

**Step 3: Add pharmacy and inventory workflows where needed**

**Step 4: Add operational reports and audit capabilities**

### Task 6: Engineering Enablement (P2)

**Files:**
- Modify: `frontend/package.json`
- Modify: `backend/pom.xml`
- Create: CI, environment, and developer workflow documents or scripts as needed

**Step 1: Add frontend automated test coverage beyond Node mapping scripts**

**Step 2: Add backend integration and API-level tests**

**Step 3: Add CI checks for build, tests, and quality gates**

**Step 4: Add deployment packaging, environment docs, monitoring, and backup planning**

### Task 7: Milestone Reviews

**Files:**
- Create or update: milestone review documents under `docs/plans/`

**Step 1: Define milestone exit criteria for each phase**

**Step 2: Reassess remaining work after each milestone rather than locking the whole roadmap too rigidly**

**Step 3: Keep the patient, doctor, admin, and engineering tracks synchronized to avoid isolated feature islands**