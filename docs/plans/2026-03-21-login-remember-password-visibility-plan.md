# Login Remember Password Visibility Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the normal login page show a clearly visible "remember password" checkbox without affecting patient mode behavior.

**Architecture:** Keep the existing login-memory behavior intact, replace the visually unstable library checkbox with a native checkbox row, and guard the change with a focused script assertion plus full frontend regression commands.

**Tech Stack:** Vue 3, Element Plus, Vite, Node.js script assertions

---

### Task 1: Lock the expected UI markers

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/frontend/scripts/test-login-default-entry.mjs`

**Step 1: Write the failing test**

Add assertions for:
- a dedicated checkbox class on the remember-password control
- explicit checkbox visual styling markers in `LoginPage.vue`

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-login-default-entry.mjs`
Expected: FAIL because the new class/style markers do not exist yet

**Step 3: Write minimal implementation**

No implementation in this step.

**Step 4: Run test to verify it still fails for the expected reason**

Run: `node ./scripts/test-login-default-entry.mjs`
Expected: FAIL on the missing remember-checkbox markers

### Task 2: Make the checkbox visually explicit

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/frontend/src/views/common/LoginPage.vue`

**Step 1: Write the failing test**

Use the failing assertions from Task 1.

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-login-default-entry.mjs`
Expected: FAIL

**Step 3: Write minimal implementation**

- Replace the remember-password control with a native checkbox row
- Add stable class names to the row, input, visual box, and label text
- Add scoped CSS so the checkbox square has explicit size, border, background, and checked-state color

**Step 4: Run test to verify it passes**

Run: `node ./scripts/test-login-default-entry.mjs`
Expected: PASS

### Task 3: Run regression verification

**Files:**
- Verify only

**Step 1: Run focused login checks**

Run: `node ./scripts/test-common-copy.mjs`
Expected: PASS

**Step 2: Run patient-mode check**

Run: `node ./scripts/test-patient-mode-access.mjs`
Expected: PASS

**Step 3: Run full frontend regression**

Run: `npm run test:ci`
Expected: PASS

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: PASS

**Step 5: Run production build**

Run: `npm run build`
Expected: PASS
