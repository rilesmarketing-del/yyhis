# Real Browser Walkthrough Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Run a real-browser walkthrough for the three demo roles, fix any demo-impacting defects, and leave behind verification evidence plus a written report.

**Architecture:** Use the existing local backend and Vite frontend as the runtime environment, then execute the highest-risk patient, doctor, and admin flows with the seeded demo accounts. For any defects found, add targeted regression coverage first where practical, implement the minimum fix, and finish with the repo's standard frontend and backend verification commands.

**Tech Stack:** Spring Boot, Vue 3, Vite, Element Plus, PowerShell, browser-based manual QA, Node.js smoke scripts, Maven tests

---

### Task 1: Prepare the walkthrough environment and acceptance checklist

**Files:**
- Create: `docs/plans/2026-03-11-real-browser-walkthrough-design.md`
- Create: `docs/plans/2026-03-11-real-browser-walkthrough-plan.md`

**Step 1: Confirm the local startup commands**

Run:
- `mvn spring-boot:run`
- `npm run dev`

Expected:
- backend serves on `http://localhost:8080`
- frontend serves on the Vite local URL

**Step 2: Confirm demo-account coverage and page order**

Use:
- patient `patient / patient123`
- doctor `doctor / doctor123`
- admin `admin / admin123`

Expected:
- each role has a deterministic walkthrough entry path

### Task 2: Execute the patient and doctor browser walkthrough

**Files:**
- Create: `docs/plans/2026-03-11-real-browser-walkthrough-report.md`
- Modify: only the files implicated by any discovered defects

**Step 1: Run the patient path in a browser**

Walk:
- login
- dashboard
- appointments
- payments
- visits
- reports
- prescriptions

Expected:
- pages load
- navigation is intact
- business identifiers and labels are suitable for demo use

**Step 2: Run the doctor path in a browser**

Walk:
- login
- dashboard
- clinic
- records
- orders
- patients
- schedule

Expected:
- doctor-only data looks scoped correctly
- visit and patient information remain coherent across pages

**Step 3: Record any defects**

Document:
- reproduction path
- severity
- expected behavior
- actual behavior

Expected:
- the report captures either clean results or actionable defects

### Task 3: Execute the admin walkthrough and fix defects with regression coverage

**Files:**
- Modify: the specific backend/frontend files implicated by findings
- Modify or create: focused regression tests near the affected area
- Modify: `docs/plans/2026-03-11-real-browser-walkthrough-report.md`

**Step 1: Run the admin path in a browser**

Walk:
- login
- dashboard
- org
- auth
- scheduling
- billing
- pharmacy
- reports
- system

Expected:
- each page loads with coherent real data
- the recent real-data pages feel consistent with the rest of the admin experience

**Step 2: For each defect, add a focused failing check first when practical**

Run examples:
- `node ./scripts/<targeted-script>.mjs`
- `mvn test "-Dtest=<TargetedIntegrationTest>"`

Expected:
- the check fails before the fix when the defect is codifiable

**Step 3: Implement the minimum fix**

Expected:
- resolve the specific walkthrough defect without widening scope

**Step 4: Re-run the targeted checks**

Expected:
- each targeted check passes after the fix

### Task 4: Run full verification and commit the walkthrough batch

**Files:**
- Verify all touched backend, frontend, and docs files

**Step 1: Run frontend smoke verification**

Run:
- `node ./scripts/test-patient-dashboard.mjs`
- `node ./scripts/test-patient-prescriptions.mjs`
- `node ./scripts/test-patient-reports.mjs`
- `node ./scripts/test-patient-visits.mjs`
- `node ./scripts/test-doctor-dashboard.mjs`
- `node ./scripts/test-doctor-orders-workbench.mjs`
- `node ./scripts/test-doctor-schedule.mjs`
- `node ./scripts/test-doctor-visit-flow.mjs`
- `node ./scripts/test-admin-dashboard.mjs`
- `node ./scripts/test-admin-org.mjs`
- `node ./scripts/test-admin-auth.mjs`
- `node ./scripts/test-admin-billing.mjs`
- `node ./scripts/test-admin-reports.mjs`
- `node ./scripts/test-admin-pharmacy-system.mjs`

Expected:
- all smoke scripts pass

**Step 2: Run backend verification**

Run:
- `mvn test`

Expected:
- all backend tests pass

**Step 3: Run production verification**

Run:
- `npm run build`
- `npm run verify:bundle`

Expected:
- frontend build succeeds
- bundle verification succeeds

**Step 4: Commit**

Run:
- `git add backend frontend docs`
- `git commit -m "Fix walkthrough issues from real browser QA"`

Expected:
- one clean commit containing the walkthrough report, any fixes, and regression coverage
