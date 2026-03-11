# Patient Dashboard Appointment Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the static patient dashboard summary and recent activity with real appointment-driven dashboard data.

**Architecture:** Keep the backend unchanged and reuse the existing `fetchMyAppointments` API. Add a small frontend mapping helper plus a Node-based regression script, then wire `PatientDashboard.vue` to the helper and `vue-router` quick actions.

**Tech Stack:** Vue 3, Vue Router, Element Plus, Vite, Node.js

---

### Task 1: Save design and plan documents

**Files:**
- Create: `docs/plans/2026-03-10-patient-dashboard-appointment-integration-design.md`
- Create: `docs/plans/2026-03-10-patient-dashboard-appointment-integration-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add a failing dashboard mapping regression script

**Files:**
- Create: `frontend/scripts/test-patient-dashboard.mjs`
- Test target: `frontend/src/services/patientDashboard.js`

**Step 1: Write a failing Node script that imports dashboard mapping helpers and asserts real appointment-derived summaries**

**Step 2: Run the script and confirm it fails because the helper module does not exist yet**

Run:
```powershell
node ./scripts/test-patient-dashboard.mjs
```

### Task 3: Implement minimal dashboard mapping helpers

**Files:**
- Create: `frontend/src/services/patientDashboard.js`
- Test: `frontend/scripts/test-patient-dashboard.mjs`

**Step 1: Implement summary card mapping**

**Step 2: Implement recent timeline mapping**

**Step 3: Implement quick action subtitle mapping**

**Step 4: Run the regression script and confirm it passes**

Run:
```powershell
node ./scripts/test-patient-dashboard.mjs
```

### Task 4: Wire the patient dashboard page to real data

**Files:**
- Modify: `frontend/src/views/patient/PatientDashboard.vue`
- Reuse: `frontend/src/services/patientAppointments.js`
- Reuse: `frontend/src/services/patientSession.js`
- Reuse: `frontend/src/services/patientDashboard.js`
- Reuse: `frontend/src/router/index.js`

**Step 1: Load the active patient and their appointment list on mount**

**Step 2: Replace static cards and timeline with mapped dashboard data**

**Step 3: Replace static quick-action buttons with real router navigation and dynamic subtitles**

**Step 4: Keep loading and empty states readable on both desktop and mobile**

### Task 5: Verify the integrated dashboard

**Files:**
- Modify if needed: `frontend/*`

**Step 1: Re-run the dashboard mapping regression script**

```powershell
node ./scripts/test-patient-dashboard.mjs
```

**Step 2: Run the frontend production build**

```powershell
npm run build
```

**Step 3: Run the bundle-size verification**

```powershell
npm run verify:bundle
```

**Step 4: Summarize any remaining demo-only gaps separately from the implemented dashboard behavior**