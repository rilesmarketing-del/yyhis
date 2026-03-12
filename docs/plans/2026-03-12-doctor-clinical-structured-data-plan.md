# Doctor Clinical Structured Data Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Structure doctor orders, prescriptions, and reports into reusable item lists while keeping existing visit-record flows and text compatibility intact.

**Architecture:** Extend `VisitRecord` with JSON-backed structured fields, return both structured lists and legacy summary text through the existing doctor record APIs, and upgrade doctor, patient, and admin frontend consumers to prefer structured data with graceful fallback to legacy text. Follow TDD by locking backend behavior first, then service mapping, then doctor UI, then patient/admin reuse.

**Tech Stack:** Spring Boot, Spring MVC, Spring Data JPA, Jackson, Vue 3, Element Plus, Vite, Node.js

---

### Task 1: Capture Backend Structured Record Expectations

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/DoctorRecordAuthorizationIntegrationTest.java`
- Modify: doctor clinic integration tests if a more specific file exists for record save/load behavior

**Step 1: Write failing integration assertions**

Add coverage for:

- saving structured `doctorOrders`, `prescriptions`, and `reports`
- reading those lists back from the doctor record endpoint
- verifying legacy text summary fields are also populated
- reading an old-style text-only record and getting fallback list items

**Step 2: Run the focused backend test**

Run: `mvn test "-Dtest=DoctorRecordAuthorizationIntegrationTest"`
Expected: FAIL because structured fields do not exist yet

**Step 3: Keep scope tight**

Do not expand to unrelated clinic flows in this task.

### Task 2: Add Backend Structured DTOs and Entity Fields

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/DoctorOrderItemDto.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/PrescriptionItemDto.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/ReportItemDto.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/model/VisitRecord.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/VisitRecordRequest.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/dto/VisitRecordResponse.java`

**Step 1: Add minimal DTO fields**

`DoctorOrderItemDto`:
- `id`
- `category`
- `content`
- `priority`

`PrescriptionItemDto`:
- `id`
- `drugName`
- `dosage`
- `frequency`
- `days`
- `remark`

`ReportItemDto`:
- `id`
- `itemName`
- `resultSummary`
- `resultFlag`
- `advice`

**Step 2: Extend `VisitRecord`**

Add:
- `doctorOrdersJson`
- `prescriptionsJson`
- `reportsJson`

Keep old text fields untouched.

**Step 3: Extend request/response DTOs**

Add list fields for the three structured collections.

**Step 4: Re-run focused backend test**

Run: `mvn test "-Dtest=DoctorRecordAuthorizationIntegrationTest"`
Expected: compile succeeds, behavior still fails in service logic

### Task 3: Implement Backend Serialization and Fallback Logic

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/DoctorClinicService.java`
- Modify: supporting mapper/helper classes if needed

**Step 1: Add JSON serialization helpers**

Use Jackson to:
- serialize structured lists to JSON strings
- deserialize JSON strings to typed DTO lists
- return empty lists on blank values
- gracefully fall back on parse failure

**Step 2: Implement summary text generation**

Generate concise text summaries from structured lists for:
- `doctorOrderNote`
- `prescriptionNote`
- `reportNote`

**Step 3: Implement fallback list generation**

When JSON fields are blank, derive minimal list items from old text fields.

**Step 4: Wire request save and response mapping**

Update save/read paths so doctor record APIs always expose structured fields.

**Step 5: Run focused backend test**

Run: `mvn test "-Dtest=DoctorRecordAuthorizationIntegrationTest"`
Expected: PASS

### Task 4: Add Frontend Mapping Coverage for Structured Clinical Data

**Files:**
- Modify: `frontend/src/services/doctorClinic.js`
- Modify: `frontend/src/services/patientReports.js`
- Modify: `frontend/src/services/patientPrescriptions.js`
- Modify: `frontend/src/services/adminPharmacy.js`
- Create: `frontend/scripts/test-doctor-clinical-structured-data.mjs`

**Step 1: Add normalization helpers**

Create helper functions to:
- normalize structured order items
- normalize prescription items
- normalize report items
- generate fallback items when only legacy text exists

**Step 2: Write the failing frontend script**

Assert:
- doctor clinic service exposes structured draft builders
- patient report mapping uses structured report items
- patient prescription mapping uses structured prescription items
- admin pharmacy mapping uses structured prescription summaries

**Step 3: Run the frontend script to verify it fails**

Run: `node ./scripts/test-doctor-clinical-structured-data.mjs`
Expected: FAIL before UI/service updates are complete

### Task 5: Upgrade Doctor Records and Orders UI

**Files:**
- Modify: `frontend/src/views/doctor/DoctorRecords.vue`
- Modify: `frontend/src/views/doctor/DoctorOrders.vue`
- Modify: `frontend/src/services/doctorClinic.js`

**Step 1: Replace text-only order/prescription/report editing with item editors**

Add minimal add/remove item interactions for:
- doctor orders
- prescriptions
- reports

**Step 2: Keep complete record and quick workbench roles distinct**

- `DoctorRecords.vue`: full editor
- `DoctorOrders.vue`: current in-progress workbench

**Step 3: Reuse one payload shape**

Both pages must save the same structured fields to the same API.

**Step 4: Run the frontend structured-data script**

Run: `node ./scripts/test-doctor-clinical-structured-data.mjs`
Expected: PASS or fail only on downstream patient/admin mapping gaps

### Task 6: Upgrade Patient and Admin Consumers

**Files:**
- Modify: `frontend/src/services/patientReports.js`
- Modify: `frontend/src/views/patient/PatientReports.vue` if display changes are needed
- Modify: `frontend/src/services/patientPrescriptions.js`
- Modify: `frontend/src/views/patient/PatientPrescriptions.vue` if display changes are needed
- Modify: `frontend/src/services/adminPharmacy.js`
- Modify: `frontend/src/views/admin/AdminPharmacy.vue` if display changes are needed

**Step 1: Prefer structured prescription data**

Patient prescriptions and admin pharmacy should use structured prescription items first.

**Step 2: Prefer structured report data**

Patient reports should use structured report items first while keeping the existing appointment fallback.

**Step 3: Keep graceful fallback for old records**

Do not remove legacy text-based mapping paths.

**Step 4: Re-run the frontend structured-data script**

Run: `node ./scripts/test-doctor-clinical-structured-data.mjs`
Expected: PASS

### Task 7: Run Full Verification

**Files:**
- No code changes expected

**Step 1: Run frontend structured-data script**

Run: `node ./scripts/test-doctor-clinical-structured-data.mjs`
Expected: PASS

**Step 2: Run relevant existing doctor and patient scripts**

Run:
- `node ./scripts/test-doctor-orders-workbench.mjs`
- `node ./scripts/test-patient-reports.mjs`
- `node ./scripts/test-patient-prescriptions.mjs`
- `node ./scripts/test-admin-pharmacy-system.mjs`

Expected: PASS

**Step 3: Run frontend build**

Run: `npm run build`
Expected: PASS

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: PASS

**Step 5: Run backend regression**

Run: `mvn test`
Expected: PASS

**Step 6: Commit**

Recommended:

```bash
git add backend/src/main/java/com/hospital/patientappointments/model/VisitRecord.java \
  backend/src/main/java/com/hospital/patientappointments/dto/VisitRecordRequest.java \
  backend/src/main/java/com/hospital/patientappointments/dto/VisitRecordResponse.java \
  backend/src/main/java/com/hospital/patientappointments/dto/DoctorOrderItemDto.java \
  backend/src/main/java/com/hospital/patientappointments/dto/PrescriptionItemDto.java \
  backend/src/main/java/com/hospital/patientappointments/dto/ReportItemDto.java \
  backend/src/main/java/com/hospital/patientappointments/service/DoctorClinicService.java \
  backend/src/test/java/com/hospital/patientappointments/integration/DoctorRecordAuthorizationIntegrationTest.java \
  frontend/src/services/doctorClinic.js \
  frontend/src/views/doctor/DoctorRecords.vue \
  frontend/src/views/doctor/DoctorOrders.vue \
  frontend/src/services/patientReports.js \
  frontend/src/services/patientPrescriptions.js \
  frontend/src/services/adminPharmacy.js \
  frontend/scripts/test-doctor-clinical-structured-data.mjs \
  docs/plans/2026-03-12-doctor-clinical-structured-data-design.md \
  docs/plans/2026-03-12-doctor-clinical-structured-data-plan.md
git commit -m "Structure doctor clinical workbench data"
```