# Admin Billing Minimum Real View Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the static admin billing page with a minimum real, read-only billing center backed by appointment payment records.

**Architecture:** Add a new admin billing overview endpoint that summarizes existing appointment records into unpaid, paid, and refunded bill counts plus a bill table. On the frontend, add a dedicated billing service that maps the response into summary cards and table rows, then update the admin billing page to consume that model instead of static mock arrays.

**Tech Stack:** Spring Boot, Spring Data JPA, Vue 3, Vite, Element Plus, Node.js script-based frontend tests

---

### Task 1: Add failing tests for the new billing overview flow

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/integration/AdminBillingIntegrationTest.java`
- Create: `frontend/scripts/test-admin-billing.mjs`

**Step 1: Write the failing tests**

- Assert `GET /api/admin/billing/overview` returns unpaid, paid, and refunded counts plus bill rows from real appointment activity
- Assert non-admin users cannot access the billing overview endpoint
- Assert the frontend billing model maps cards and bill rows into Chinese admin billing copy
- Assert the billing view consumes the new model fields instead of static mock arrays

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-admin-billing.mjs`
Expected: fail because the admin billing service and model do not exist yet

Run: `mvn test "-Dtest=AdminBillingIntegrationTest"`
Expected: fail because the admin billing endpoint does not exist yet

### Task 2: Implement the backend admin billing overview endpoint

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/controller/AdminBillingController.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/AdminBillingService.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/AdminBillingOverviewResponse.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/AppointmentRecordRepository.java` only if a helper query is needed

**Step 1: Write minimal implementation**

- Add `GET /api/admin/billing/overview`
- Reuse appointment records as billing rows
- Return unpaid, paid, and refunded counts
- Sort bills by creation time descending
- Reuse the existing appointment response shape for each bill row where practical

**Step 2: Run test to verify it passes**

Run: `mvn test "-Dtest=AdminBillingIntegrationTest"`
Expected: pass

### Task 3: Implement the frontend billing model and real page

**Files:**
- Create: `frontend/src/services/adminBilling.js`
- Modify: `frontend/src/views/admin/AdminBilling.vue`

**Step 1: Write minimal implementation**

- Fetch `/api/admin/billing/overview`
- Build cards for `待支付账单`、`已支付账单`、`已退款账单`
- Build bill rows with Chinese status labels and fee formatting
- Replace static tables and fake action buttons with the real read-only billing table
- Add a small explanatory card stating the current page reflects挂号预约收费 records

**Step 2: Run test to verify it passes**

Run: `node ./scripts/test-admin-billing.mjs`
Expected: pass

### Task 4: Final verification

**Files:**
- Verify all modified backend and frontend files

**Step 1: Run backend verification**

Run: `mvn test "-Dtest=AdminBillingIntegrationTest,AdminOperationsReportIntegrationTest"`
Expected: pass

**Step 2: Run frontend verification**

Run: `node ./scripts/test-admin-billing.mjs`
Expected: pass

**Step 3: Run production build verification**

Run: `npm run build`
Expected: build success

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: all bundles within configured threshold