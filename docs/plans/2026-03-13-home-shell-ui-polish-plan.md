# Home And Shell UI Polish Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Apply a unified warm medical operations-hub visual polish to the login page, shared shell, and the patient/doctor/admin dashboards.

**Architecture:** Keep all routing and data flow intact. The implementation is a focused presentation pass across five Vue files plus one light regression check. Styling should be shared in spirit rather than extracted into a new design system layer unless repetition becomes unavoidable.

**Tech Stack:** Vue 3 single-file components, Element Plus, Vite, existing Node.js frontend script checks.

---

### Task 1: Add a lightweight visual regression guard

**Files:**
- Create: `frontend/scripts/test-home-shell-ui-polish.mjs`

**Step 1: Write the failing test**

Create a script that reads the target Vue files and checks for a small set of theme markers/classes/copy anchors that only exist after the polish pass.

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-home-shell-ui-polish.mjs`
Expected: FAIL because the new theme markers do not exist yet.

**Step 3: Commit**

```bash
git add frontend/scripts/test-home-shell-ui-polish.mjs
git commit -m "test: guard home shell ui polish"
```

### Task 2: Polish the shared shell and login entry

**Files:**
- Modify: `frontend/src/layout/MainLayout.vue`
- Modify: `frontend/src/views/common/LoginPage.vue`

**Step 1: Implement shell polish**

Update the shell header, sidebar surface, and content framing to establish the new warm medical hub design.

**Step 2: Implement login polish**

Refine the login hero and form card presentation while keeping the current login/register behavior unchanged.

**Step 3: Run the focused visual test**

Run: `node ./scripts/test-home-shell-ui-polish.mjs`
Expected: partial or full PASS depending on covered markers.

**Step 4: Commit**

```bash
git add frontend/src/layout/MainLayout.vue frontend/src/views/common/LoginPage.vue frontend/scripts/test-home-shell-ui-polish.mjs
git commit -m "style: polish shell and login entry"
```

### Task 3: Polish the three role dashboards

**Files:**
- Modify: `frontend/src/views/patient/PatientDashboard.vue`
- Modify: `frontend/src/views/doctor/DoctorDashboard.vue`
- Modify: `frontend/src/views/admin/AdminDashboard.vue`

**Step 1: Implement patient dashboard polish**

Emphasize warmth, guidance, and softer card treatment.

**Step 2: Implement doctor dashboard polish**

Emphasize clarity, action density, and workbench feel.

**Step 3: Implement admin dashboard polish**

Emphasize operational oversight, KPI emphasis, and alert clarity.

**Step 4: Re-run the focused visual test**

Run: `node ./scripts/test-home-shell-ui-polish.mjs`
Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/patient/PatientDashboard.vue frontend/src/views/doctor/DoctorDashboard.vue frontend/src/views/admin/AdminDashboard.vue
 git commit -m "style: polish role dashboards"
```

### Task 4: Verify the full frontend surface

**Files:**
- Modify: `frontend/package.json` only if the new visual guard should be included in `test:ci`

**Step 1: Add the new guard to the aggregate script if appropriate**

Include `test-home-shell-ui-polish.mjs` in `npm run test:ci` if it stays stable and low-noise.

**Step 2: Run frontend verification**

Run:
- `npm run test:ci`
- `npm run build`
- `npm run verify:bundle`
Expected: PASS.

**Step 3: Commit**

```bash
git add frontend/package.json
 git commit -m "build: include ui polish guard"
```