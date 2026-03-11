# Patient Visits Appointment Mapping Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the static patient visits page with a real view over paid appointment records.

**Architecture:** Reuse the existing appointment API and add a small frontend mapping helper plus a Node-based regression script. The page will fetch appointments for the active patient, derive visit rows from paid appointments, and render search plus a detail drawer.

**Tech Stack:** Vue 3, Vite, Element Plus, Vue Router, Node.js

---

### Task 1: Save design and plan documents

**Files:**
- Create: `docs/plans/2026-03-10-patient-visits-appointment-mapping-design.md`
- Create: `docs/plans/2026-03-10-patient-visits-appointment-mapping-plan.md`

**Step 1: Save the approved design**

**Step 2: Save this implementation plan**

### Task 2: Add a failing visit mapping regression script

**Files:**
- Create: `frontend/scripts/test-patient-visits.mjs`
- Test target: `frontend/src/services/patientVisits.js`

**Step 1: Write a failing script that asserts only paid appointments become visit rows**

**Step 2: Run it and confirm it fails because the helper module does not exist yet**

Run:
```powershell
node ./scripts/test-patient-visits.mjs
```

### Task 3: Implement the visit mapping helper

**Files:**
- Create: `frontend/src/services/patientVisits.js`
- Test: `frontend/scripts/test-patient-visits.mjs`

**Step 1: Map paid appointments into visit rows**

**Step 2: Add next-step suggestion rules**

**Step 3: Re-run the regression script and confirm it passes**

Run:
```powershell
node ./scripts/test-patient-visits.mjs
```

### Task 4: Wire the patient visits page to real data

**Files:**
- Modify: `frontend/src/views/patient/PatientVisits.vue`
- Reuse: `frontend/src/services/patientAppointments.js`
- Reuse: `frontend/src/services/patientSession.js`
- Reuse: `frontend/src/services/patientVisits.js`

**Step 1: Load the active patient and fetch appointments on mount**

**Step 2: Replace static rows with mapped visit rows**

**Step 3: Keep keyword search and the detail drawer working on mapped data**

**Step 4: Add an empty state with navigation to appointments and payments**

### Task 5: Verify the integrated page

**Files:**
- Modify if needed: `frontend/*`

**Step 1: Re-run the visit mapping regression script**

```powershell
node ./scripts/test-patient-visits.mjs
```

**Step 2: Run the frontend build**

```powershell
npm run build
```

**Step 3: Run the bundle-size verification**

```powershell
npm run verify:bundle
```