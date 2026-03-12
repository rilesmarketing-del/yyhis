# Patient Registration Handoff Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Let newly registered patients automatically sign in, land on the patient dashboard, and see a one-time next-step guide that points them to appointment booking.

**Architecture:** Keep the backend registration contract unchanged and implement the handoff entirely in the frontend. Reuse the existing `login()` flow after successful registration, store a one-time onboarding hint in `patientSession.js`, and let `PatientDashboard.vue` consume and clear that hint while preserving the existing real-data dashboard.

**Tech Stack:** Vue 3, Vue Router, Element Plus, Vite, Node.js, Spring Boot regression verification

---

### Task 1: Capture Frontend Handoff Expectations with Failing Scripts

**Files:**
- Modify: `frontend/scripts/test-patient-dashboard.mjs`
- Create: `frontend/scripts/test-patient-registration-handoff.mjs`

**Step 1: Write the failing dashboard assertions**

Extend `test-patient-dashboard.mjs` so it asserts the presence of first-entry onboarding behavior, either through a new model helper or direct copy expectations tied to the dashboard guide.

**Step 2: Write the failing login handoff script**

Create `test-patient-registration-handoff.mjs` to assert:

- `LoginPage.vue` calls `login()` after successful registration
- registration success writes onboarding state before redirect
- login fallback keeps credentials available for manual sign-in
- `patientSession.js` exposes onboarding state helpers

**Step 3: Run scripts to verify they fail**

Run:
- `node ./scripts/test-patient-registration-handoff.mjs`
- `node ./scripts/test-patient-dashboard.mjs`

Expected: FAIL because the onboarding flow does not exist yet

### Task 2: Add Onboarding State Helpers

**Files:**
- Modify: `frontend/src/services/patientSession.js`

**Step 1: Add one-time onboarding state helpers**

Implement minimal helpers such as:

- `setRegistrationOnboarding(data)`
- `getRegistrationOnboarding()`
- `clearRegistrationOnboarding()`

Keep them defensive if `sessionStorage` is unavailable.

**Step 2: Preserve existing patient identity helpers**

Do not break:

- `getActivePatient()`
- `setActivePatient()`
- `findPatientById()`

**Step 3: Run handoff script**

Run: `node ./scripts/test-patient-registration-handoff.mjs`
Expected: still FAIL because login page and dashboard are not wired yet

### Task 3: Implement Registration Auto-Login Handoff

**Files:**
- Modify: `frontend/src/views/common/LoginPage.vue`

**Step 1: Update registration success flow**

After `registerPatient()` succeeds:

- call `login()` with the newly registered credentials
- set the onboarding state using the returned current user plus register response
- close the dialog
- redirect to `roleMeta.patient.homePath`

**Step 2: Implement fallback behavior**

If auto-login fails:

- close the dialog
- refill `form.username` and `form.password`
- show a success-or-warning message that the account was created and manual login is available

**Step 3: Keep login page validation minimal**

Do not introduce extra rules beyond the current register form validation.

**Step 4: Run handoff script**

Run: `node ./scripts/test-patient-registration-handoff.mjs`
Expected: PASS or fail only on dashboard onboarding assertions

### Task 4: Add the One-Time Welcome Guide on Patient Dashboard

**Files:**
- Modify: `frontend/src/views/patient/PatientDashboard.vue`
- Modify: `frontend/src/services/patientDashboard.js` if a helper model improves clarity

**Step 1: Render onboarding card only for fresh registrations**

Show a lightweight card above the existing dashboard stats when onboarding data is present.

**Step 2: Add next-step action**

Expose a primary action that routes to `/patient/appointments`.

**Step 3: Consume onboarding state**

Clear the onboarding hint when the user dismisses the card or after the first page consumption flow is complete.

**Step 4: Preserve current real-data loading**

Do not regress:

- dashboard stats
- timeline
- quick actions
- error handling in `loadDashboard()`

**Step 5: Run dashboard script**

Run: `node ./scripts/test-patient-dashboard.mjs`
Expected: PASS

### Task 5: Run Full Verification

**Files:**
- No code changes expected

**Step 1: Run the new handoff script**

Run: `node ./scripts/test-patient-registration-handoff.mjs`
Expected: PASS

**Step 2: Run the patient dashboard script**

Run: `node ./scripts/test-patient-dashboard.mjs`
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
git add frontend/src/views/common/LoginPage.vue \
  frontend/src/views/patient/PatientDashboard.vue \
  frontend/src/services/patientSession.js \
  frontend/src/services/patientDashboard.js \
  frontend/scripts/test-patient-registration-handoff.mjs \
  frontend/scripts/test-patient-dashboard.mjs \
  docs/plans/2026-03-12-patient-registration-handoff-design.md \
  docs/plans/2026-03-12-patient-registration-handoff-plan.md
git commit -m "Improve patient registration handoff"
```