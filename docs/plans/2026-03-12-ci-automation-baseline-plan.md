# CI Automation Baseline Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a first GitHub Actions CI workflow that automatically validates backend tests and a curated frontend regression/build set on push and pull request.

**Architecture:** Use a single workflow file with separate backend and frontend jobs. Keep the first version small and stable by reusing existing verification commands and adding a frontend aggregate CI script rather than introducing new tooling.

**Tech Stack:** GitHub Actions, Maven, Node.js, npm, Vite

---

### Task 1: Add frontend aggregate CI script

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/frontend/package.json`

**Step 1: Write the failing expectation**

Define the target script name and command list in the plan before editing:

- `test:ci`
- Run these commands in order:
  - `node ./scripts/test-patient-registration-handoff.mjs`
  - `node ./scripts/test-patient-dashboard.mjs`
  - `node ./scripts/test-doctor-orders-workbench.mjs`
  - `node ./scripts/test-doctor-clinical-structured-data.mjs`
  - `node ./scripts/test-admin-org-governance.mjs`
  - `node ./scripts/test-admin-pharmacy-system.mjs`

**Step 2: Implement the minimal change**

Add `test:ci` under `scripts` in `frontend/package.json`.

**Step 3: Run the new script**

Run: `npm run test:ci`
Expected: all curated frontend smoke scripts pass.

**Step 4: Commit checkpoint**

Do not commit yet if later tasks are tightly coupled, but keep the diff isolated and reviewable.

### Task 2: Add GitHub Actions workflow

**Files:**
- Create: `C:/Users/89466/Desktop/yy/.github/workflows/ci.yml`

**Step 1: Write the workflow skeleton**

Create one workflow named clearly, triggered on:

- `push`
- `pull_request`

Add jobs:

- `backend`
- `frontend`

**Step 2: Implement backend job**

Use:

- `actions/checkout@v5`
- `actions/setup-java@v5`

Configure:

- Java 15
- distribution `temurin`
- Maven cache enabled

Run in `backend` directory:

- `mvn test`

**Step 3: Implement frontend job**

Use:

- `actions/checkout@v5`
- `actions/setup-node@v4`

Configure:

- Node 20
- npm cache using `frontend/package-lock.json`

Run in `frontend` directory:

- `npm ci`
- `npm run test:ci`
- `npm run build`
- `npm run verify:bundle`

**Step 4: Add job timeouts**

Set a reasonable timeout for both jobs.

Suggested starting point:

- `backend`: 15 minutes
- `frontend`: 15 minutes

### Task 3: Add short CI usage documentation

**Files:**
- Check existing: `C:/Users/89466/Desktop/yy/README.md`
- Create or modify depending on presence:
  - `C:/Users/89466/Desktop/yy/README.md`

**Step 1: Inspect current docs state**

If no root README exists, add a short one focused only on local verification and CI coverage.

**Step 2: Document CI behavior**

Include:

- Workflow trigger conditions
- Backend command: `mvn test`
- Frontend commands: `npm run test:ci`, `npm run build`, `npm run verify:bundle`

Keep this concise.

### Task 4: Verify locally before completion

**Files:**
- No new files

**Step 1: Run frontend curated CI script**

Run in `frontend`:

- `npm run test:ci`

Expected: pass

**Step 2: Run frontend build checks**

Run in `frontend`:

- `npm run build`
- `npm run verify:bundle`

Expected: pass

**Step 3: Run backend full tests**

Run in `backend`:

- `mvn test`

Expected: pass

**Step 4: Review workflow file**

Read back `C:/Users/89466/Desktop/yy/.github/workflows/ci.yml` and confirm:

- triggers are correct
- working directories are correct
- cache settings are valid
- command order matches validated local flow

### Task 5: Prepare clean commit

**Files:**
- Stage only the CI-related files for this task

**Step 1: Check git status**

Confirm only expected files are modified.

**Step 2: Commit**

Suggested commit message:

- `Add GitHub Actions CI baseline`
