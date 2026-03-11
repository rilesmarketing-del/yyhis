# Patient Reports Appointment Mapping Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the static patient reports page with a report placeholder view derived from eligible paid appointments.

**Architecture:** Reuse the existing appointment API and add a small frontend mapping helper plus a Node-based regression script. The page will fetch appointments for the active patient, derive report rows from paid-and-due appointments, and preserve search plus preview interactions.

**Tech Stack:** Vue 3, Vite, Element Plus, Vue Router, Node.js

---

### Task 1: Save design and plan documents

**Files:**
- Create: `docs/plans/2026-03-10-patient-reports-appointment-mapping-design.md`
- Create: `docs/plans/2026-03-10-patient-reports-appointment-mapping-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add a failing report mapping regression script

**Files:**
- Create: `frontend/scripts/test-patient-reports.mjs`
- Test target: `frontend/src/services/patientReports.js`

**Step 1: Write a failing script that asserts only due and paid appointments become report rows**

**Step 2: Run it and confirm it fails because the helper module does not exist yet**

Run:
```powershell
node ./scripts/test-patient-reports.mjs
```

### Task 3: Implement the report mapping helper

**Files:**
- Create: `frontend/src/services/patientReports.js`
- Test: `frontend/scripts/test-patient-reports.mjs`

**Step 1: Map eligible appointments into report rows**

**Step 2: Add item, result, summary, and advice derivation rules**

**Step 3: Re-run the regression script and confirm it passes**

Run:
```powershell
node ./scripts/test-patient-reports.mjs
```

### Task 4: Wire the patient reports page to real data

**Files:**
- Modify: `frontend/src/views/patient/PatientReports.vue`
- Reuse: `frontend/src/services/patientAppointments.js`
- Reuse: `frontend/src/services/patientSession.js`
- Reuse: `frontend/src/services/patientReports.js`

**Step 1: Load the active patient and fetch appointments on mount**

**Step 2: Replace static rows with mapped report rows**

**Step 3: Keep search and preview dialog working on mapped data**

**Step 4: Add an empty state with navigation to visits and appointments**

### Task 5: Verify the integrated page

**Files:**
- Modify if needed: `frontend/*`

**Step 1: Re-run the report mapping regression script**

```powershell
node ./scripts/test-patient-reports.mjs
```

**Step 2: Run the frontend build**

```powershell
npm run build
```

**Step 3: Run the bundle-size verification**

```powershell
npm run verify:bundle
```