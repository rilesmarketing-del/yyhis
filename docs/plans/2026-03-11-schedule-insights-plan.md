# Schedule Insights Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add lightweight schedule insights to the admin and doctor schedule pages so users can see totals, near-capacity risk, and the next relevant schedule without opening each row.

**Architecture:** Keep all new insight logic in frontend service helpers so the pages render summary cards and risk tags from existing schedule payloads without any backend API changes. Extend the existing schedule regression script first, then update the services and Vue views to consume the new derived model fields.

**Tech Stack:** Vue 3, Vite, Element Plus, Node.js script-based frontend regression tests

---

### Task 1: Lock the new insight requirements with a failing frontend test

**Files:**
- Modify: `frontend/scripts/test-doctor-schedule.mjs`
- Test: `frontend/src/services/adminSchedules.js`
- Test: `frontend/src/services/doctorSchedule.js`
- Test: `frontend/src/views/admin/AdminScheduling.vue`
- Test: `frontend/src/views/doctor/DoctorSchedule.vue`

**Step 1: Write the failing test**

- Assert admin-side summary labels render `排班总数`、`今日排班`、`紧张号源`
- Assert admin-side capacity helper distinguishes `充足`、`紧张`、`已约满`、`已停用`
- Assert doctor-side model returns `我的排班数`、`本周排班`、`最近一班`
- Assert doctor-side empty hint tells the doctor to contact the admin when no schedules exist

**Step 2: Run test to verify it fails**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: fail because the new helpers, summary values, and template copy do not exist yet

### Task 2: Implement admin-side schedule insights

**Files:**
- Modify: `frontend/src/services/adminSchedules.js`
- Modify: `frontend/src/views/admin/AdminScheduling.vue`

**Step 1: Write minimal implementation**

- Add a pure helper that derives summary cards from the schedule list
- Add a pure helper for row-level capacity status tags
- Render summary cards above the admin schedule table
- Show a clearer empty state and make low remaining-slot schedules visually obvious

**Step 2: Run test to verify it passes**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: admin-side schedule insight assertions pass

### Task 3: Implement doctor-side schedule insights

**Files:**
- Modify: `frontend/src/services/doctorSchedule.js`
- Modify: `frontend/src/views/doctor/DoctorSchedule.vue`

**Step 1: Write minimal implementation**

- Extend the doctor schedule model with summary cards and a stronger empty hint
- Compute the current-week schedule count and nearest upcoming schedule from existing rows
- Render summary cards in the doctor schedule page without changing the read-only nature of the page

**Step 2: Run test to verify it passes**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: doctor-side summary and empty-state assertions pass

### Task 4: Final verification

**Files:**
- Verify all modified frontend files

**Step 1: Run targeted regression**

Run: `node ./scripts/test-doctor-schedule.mjs`
Expected: pass

**Step 2: Run production build verification**

Run: `npm run build`
Expected: build success

**Step 3: Run bundle verification**

Run: `npm run verify:bundle`
Expected: all bundles within configured threshold