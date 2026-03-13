# Frontend Copy And CI Sanitization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Prove the shared frontend copy is healthy, refresh stale frontend regression scripts, and align `npm run test:ci` with the maintained script inventory.

**Architecture:** Treat the apparent mojibake as a verification problem first, not a production bug. Add a new copy guard, refresh outdated admin-org script expectations, and aggregate the full maintained script set into the frontend CI command. Leave production Vue files untouched unless the new tests actually expose a real source regression.

**Tech Stack:** Vue 3, Vite, Node.js script-based frontend regression checks.

---

### Task 1: Add source-level copy verification

**Files:**
- Create: `frontend/scripts/test-common-copy.mjs`
- Modify: `frontend/scripts/test-admin-org.mjs`
- Modify: `frontend/scripts/test-admin-org-governance.mjs`

**Step 1: Write the focused copy guard**

Create `frontend/scripts/test-common-copy.mjs` that imports `roleMeta` and `roleMenus`, reads `frontend/src/layout/MainLayout.vue`, `frontend/src/views/common/LoginPage.vue`, and `frontend/src/services/auth.js`, and asserts readable Chinese labels/copy markers.

**Step 2: Refresh stale admin-org expectations**

Update `frontend/scripts/test-admin-org.mjs` and `frontend/scripts/test-admin-org-governance.mjs` so they assert the current readable Chinese labels and formatting.

**Step 3: Run the focused scripts**

Run:
- `node ./scripts/test-common-copy.mjs`
- `node ./scripts/test-admin-org.mjs`
- `node ./scripts/test-admin-org-governance.mjs`
Expected: PASS, proving the current source is healthy and the scripts now reflect real behavior.

**Step 4: Commit**

```bash
git add frontend/scripts/test-common-copy.mjs frontend/scripts/test-admin-org.mjs frontend/scripts/test-admin-org-governance.mjs
git commit -m "test: refresh frontend copy guards"
```

### Task 2: Widen frontend CI aggregation

**Files:**
- Modify: `frontend/package.json`

**Step 1: Update the aggregate script**

Extend `test:ci` to include the maintained frontend script set:
- `test-common-copy`
- patient dashboard/detail flows
- doctor dashboard/detail flows
- admin dashboard/detail flows

**Step 2: Run aggregate verification**

Run: `npm run test:ci`
Expected: PASS with the widened script inventory.

**Step 3: Run frontend build verification**

Run:
- `npm run build`
- `npm run verify:bundle`
Expected: PASS.

**Step 4: Commit**

```bash
git add frontend/package.json
git commit -m "build: widen frontend ci checks"
```