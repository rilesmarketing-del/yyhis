# Admin Pharmacy and System Minimum Real Views Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the static admin pharmacy and system pages with lightweight read-only views backed by real prescription, dashboard, report, and organization data.

**Architecture:** Add one new backend pharmacy overview endpoint derived from visit records with prescription text. On the frontend, add dedicated admin pharmacy and admin system services that map backend data into summary cards and read-only tables, then replace the two static views with real overview pages.

**Tech Stack:** Spring Boot, Spring Data JPA, Vue 3, Vite, Element Plus, Node.js script-based frontend tests

---

### Task 1: Add failing tests for pharmacy and system real-data flows

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AdminPharmacyIntegrationTest.java`
- Create: `frontend/scripts/test-admin-pharmacy-system.mjs`

**Step 1: Write the failing tests**

- Assert `GET /api/admin/pharmacy/overview` returns only visits that contain prescription text, plus the expected summary counts
- Assert non-admin users cannot access the pharmacy overview endpoint
- Assert the frontend pharmacy model formats cards, record rows, and explanatory copy correctly
- Assert the frontend system model combines dashboard summary, operations report, and org summary into cards, role stats, metrics, and reminders
- Assert both admin views consume the new model services instead of static demo arrays

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-pharmacy-system.mjs`
Expected: fail because the pharmacy/system services do not exist yet

Run: `mvn test "-Dtest=AdminPharmacyIntegrationTest"`
Expected: fail because the pharmacy overview endpoint does not exist yet

### Task 2: Implement the backend admin pharmacy overview endpoint

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/AdminPharmacyController.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/AdminPharmacyService.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/AdminPharmacyOverviewResponse.java`

**Step 1: Write minimal implementation**

- Add `GET /api/admin/pharmacy/overview`
- Filter visit records down to rows with non-blank `prescriptionNote`
- Count total prescription records, today prescription records, and distinct patients
- Return rows sorted by `updatedAt` descending
- Reuse existing visit data where practical instead of inventing a deeper pharmacy model

**Step 2: Run test to verify it passes**

Run: `mvn test "-Dtest=AdminPharmacyIntegrationTest"`
Expected: pass

### Task 3: Implement the frontend pharmacy and system models plus real pages

**Files:**
- Create: `frontend/src/services/adminPharmacy.js`
- Create: `frontend/src/services/adminSystem.js`
- Modify: `frontend/src/views/admin/AdminPharmacy.vue`
- Modify: `frontend/src/views/admin/AdminSystem.vue`

**Step 1: Write minimal implementation**

- Fetch the new pharmacy overview endpoint
- Build pharmacy cards and prescription record rows with Chinese labels
- Build the system page model from the existing dashboard, report, and org endpoints
- Replace the static inventory and config page scaffolds with real read-only overview layouts
- Add explanatory notes clarifying the current phase boundaries

**Step 2: Run test to verify it passes**

Run: `node ./scripts/test-admin-pharmacy-system.mjs`
Expected: pass

### Task 4: Final verification

**Files:**
- Verify all modified backend and frontend files

**Step 1: Run backend verification**

Run: `mvn test`
Expected: pass

**Step 2: Run frontend targeted verification**

Run: `node ./scripts/test-admin-pharmacy-system.mjs`
Expected: pass

**Step 3: Run production build verification**

Run: `npm run build`
Expected: build success

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: all bundles within configured threshold