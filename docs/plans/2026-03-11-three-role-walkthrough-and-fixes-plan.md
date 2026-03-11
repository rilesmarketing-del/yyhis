# Three-Role Walkthrough And Fixes Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Perform a practical smoke walkthrough of patient, doctor, and admin flows, then fix the first round of defects that would disrupt a demo.

**Architecture:** Use route and data-flow walkthroughs plus scriptable frontend mapping tests and backend integration tests to approximate browser-level smoke coverage in the current terminal-only environment. Prioritize defects that break credibility, misroute users, or surface internal IDs instead of user-facing business data.

**Tech Stack:** Vue 3, Vue Router, Element Plus, Node.js script tests, Spring Boot integration tests

---

### Task 1: Build the walkthrough checklist and identify high-risk demo defects

**Files:**
- Create: `docs/plans/2026-03-11-three-role-walkthrough-and-fixes-report.md`
- Inspect: `frontend/src/router/index.js`
- Inspect: `frontend/src/views/patient/*.vue`
- Inspect: `frontend/src/views/doctor/*.vue`
- Inspect: `frontend/src/views/admin/*.vue`

**Step 1: Record the three role walkthrough paths**
- Patient: login -> dashboard -> appointments -> payments -> visits -> reports -> prescriptions
- Doctor: login -> dashboard -> clinic -> records -> orders -> patients -> schedule
- Admin: login -> dashboard -> org -> auth -> scheduling -> billing -> reports -> pharmacy -> system

**Step 2: Note the first-round defects worth fixing now**
- Outdated patient dashboard copy still describes reports as a demo-only entry
- Patient visit/report views show internal appointment IDs instead of business serial numbers when real visit records exist
- Any additional route/data mismatch found during the same sweep

### Task 2: Add failing tests for the first-round walkthrough defects

**Files:**
- Modify: `frontend/scripts/test-patient-dashboard.mjs`
- Modify: `frontend/scripts/test-patient-visits.mjs`
- Modify: `frontend/scripts/test-patient-reports.mjs`

**Step 1: Write the failing tests**
- Assert the patient dashboard report quick action no longer describes the report page as a demo-only module
- Assert real visit-derived patient visits resolve the appointment serial number from matching appointments
- Assert real visit-derived patient reports resolve the appointment serial number from matching appointments

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-patient-dashboard.mjs`
Expected: fail on outdated dashboard copy

Run: `node ./scripts/test-patient-visits.mjs`
Expected: fail because real visit rows still expose internal appointment IDs

Run: `node ./scripts/test-patient-reports.mjs`
Expected: fail because real report rows still expose internal appointment IDs

### Task 3: Implement the first walkthrough fixes

**Files:**
- Modify: `frontend/src/services/patientDashboard.js`
- Modify: `frontend/src/services/patientVisits.js`
- Modify: `frontend/src/services/patientReports.js`
- Update report: `docs/plans/2026-03-11-three-role-walkthrough-and-fixes-report.md`

**Step 1: Write minimal implementation**
- Replace the outdated patient dashboard report quick-action description with real-data wording
- Build an appointment lookup in patient visit/report mapping so real visit records can show the actual appointment serial number when available
- Keep fallback behavior intact when a matching appointment is not available
- Record the walkthrough finding and fix in the report document

**Step 2: Run test to verify it passes**

Run: `node ./scripts/test-patient-dashboard.mjs`
Expected: pass

Run: `node ./scripts/test-patient-visits.mjs`
Expected: pass

Run: `node ./scripts/test-patient-reports.mjs`
Expected: pass

### Task 4: Run targeted and full verification

**Files:**
- Verify all modified frontend and backend files

**Step 1: Run focused frontend walkthrough scripts**

Run: `node ./scripts/test-patient-dashboard.mjs`
Expected: pass

Run: `node ./scripts/test-patient-visits.mjs`
Expected: pass

Run: `node ./scripts/test-patient-reports.mjs`
Expected: pass

Run: `node ./scripts/test-admin-pharmacy-system.mjs`
Expected: pass

Run: `node ./scripts/test-admin-billing.mjs`
Expected: pass

**Step 2: Run production build verification**

Run: `npm run build`
Expected: build success

**Step 3: Run backend verification**

Run: `mvn test`
Expected: pass

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: all bundles within configured threshold