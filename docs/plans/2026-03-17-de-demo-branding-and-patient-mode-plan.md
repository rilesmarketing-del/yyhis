# De-demo Branding And Patient Mode Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove demo-oriented branding from the primary frontend entry surfaces and add a browser-local patient mode that hides doctor/admin access until a real admin unlocks it.

**Architecture:** Keep the existing backend auth API and route structure intact. Implement patient mode entirely in the frontend with a dedicated local-state service, login-page mode switching, and router guard enforcement. Use focused script tests to drive copy and behavior changes before production code updates.

**Tech Stack:** Vue 3 SFCs, Vue Router, Element Plus, existing frontend script-based regression suite.

---

### Task 1: Add failing tests for patient mode and de-demo branding

**Files:**
- Create: `frontend/scripts/test-patient-mode-access.mjs`
- Modify: `frontend/scripts/test-common-copy.mjs`
- Modify: `frontend/package.json`

**Step 1: Write the failing test for patient mode behavior**

Create `frontend/scripts/test-patient-mode-access.mjs` to assert that:
- a dedicated patient mode service exists
- the login page contains patient-mode markers and an admin unlock marker
- the router contains patient-mode enforcement markers

**Step 2: Extend the common copy test with new expectations**

Update `frontend/scripts/test-common-copy.mjs` so it fails if touched surfaces still contain the old demo wording, and so it expects the new formal copy anchors.

**Step 3: Run tests to verify RED**

Run:
- `node ./scripts/test-patient-mode-access.mjs`
- `node ./scripts/test-common-copy.mjs`

Expected:
- FAIL because patient mode service/markers do not exist yet
- FAIL because current copy still contains demo wording

**Step 4: Add the new test to the aggregate script**

Update `frontend/package.json` so `test:ci` includes `test-patient-mode-access.mjs` near the common-copy/login checks.

**Step 5: Commit**

```bash
git add frontend/scripts/test-patient-mode-access.mjs frontend/scripts/test-common-copy.mjs frontend/package.json
git commit -m "test: cover patient mode and de-demo entry copy"
```

### Task 2: Implement patient mode state and router enforcement

**Files:**
- Create: `frontend/src/services/patientMode.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/services/auth.js`

**Step 1: Write the minimal patient mode service**

Add a service that:
- stores patient mode in browser `localStorage`
- exposes `isPatientModeEnabled`, `enablePatientMode`, `disablePatientMode`
- exposes a small helper for checking whether a role is blocked in patient mode

**Step 2: Update router enforcement**

Modify `frontend/src/router/index.js` so that:
- doctor/admin routes redirect to `/login` while patient mode is enabled
- blocked doctor/admin sessions are cleared before redirect
- patient routes still work normally

**Step 3: Keep auth behavior compatible**

Ensure `frontend/src/services/auth.js` exports any helper needed to clear session safely from the router and temporary unlock flow.

**Step 4: Run focused tests**

Run:
- `node ./scripts/test-patient-mode-access.mjs`

Expected:
- partial PASS or full PASS for service/router markers

**Step 5: Commit**

```bash
git add frontend/src/services/patientMode.js frontend/src/router/index.js frontend/src/services/auth.js
git commit -m "feat: enforce patient mode access boundaries"
```

### Task 3: Rework login page for formal branding and patient mode unlock

**Files:**
- Modify: `frontend/src/views/common/LoginPage.vue`

**Step 1: Remove demo wording from the login page**

Replace the current demo-oriented copy with formal product wording.

**Step 2: Add normal-mode and patient-mode rendering**

Implement two presentation states:
- normal mode with three formal access cards and patient self-registration
- patient mode with patient-only login/register and a clear status banner

**Step 3: Add patient mode toggle and admin unlock dialog**

Implement:
- a control to enable patient mode in the current browser
- an admin unlock dialog requiring username/password
- unlock flow using existing auth login API
- rejection of non-admin credentials
- no persistence of temporary unlock login as the active app session

**Step 4: Re-run focused tests**

Run:
- `node ./scripts/test-patient-mode-access.mjs`
- `node ./scripts/test-common-copy.mjs`

Expected:
- PASS

**Step 5: Commit**

```bash
git add frontend/src/views/common/LoginPage.vue
 git commit -m "feat: add patient mode login experience"
```

### Task 4: Clean shared shell branding and account identity display

**Files:**
- Modify: `frontend/src/layout/MainLayout.vue`

**Step 1: Remove top-left demo-only wording**

Delete the “Hospital Demo” and “演示工作台” style copy from the shell header.

**Step 2: Simplify account identity display**

Update the top-right account area to show:
- display name as primary
- username as secondary

Do not append “患者端 / 医生端 / 管理端” in the account identity area.

**Step 3: Keep navigation context readable**

Preserve breadcrumb or title context so users still know where they are.

**Step 4: Re-run copy tests**

Run:
- `node ./scripts/test-common-copy.mjs`

Expected:
- PASS

**Step 5: Commit**

```bash
git add frontend/src/layout/MainLayout.vue
 git commit -m "style: remove demo branding from shell"
```

### Task 5: Verify the full frontend surface

**Files:**
- Modify docs only if implementation details need follow-up notes

**Step 1: Run aggregate frontend verification**

Run:
- `npm run test:ci`
- `npm run build`
- `npm run verify:bundle`

Expected:
- PASS

**Step 2: Review touched requirements**

Confirm:
- primary entry surfaces no longer read as a demo
- doctor/admin are hidden and blocked in patient mode
- only admin credentials can unlock patient mode
- account area no longer labels the user as belonging to an “end”

**Step 3: Commit final verification-safe changes if needed**

```bash
git add <touched-files>
 git commit -m "chore: finalize patient mode entry flow"
```
