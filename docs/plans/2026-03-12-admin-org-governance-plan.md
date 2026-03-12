# Admin Org Governance Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Upgrade the admin organization page from create-only management to a real governance flow with department editing, account editing, account enable/disable, and password reset.

**Architecture:** Extend the existing admin org controller/service pair with minimal governance endpoints, reuse the existing summary response as the page refresh source, and keep the frontend on the current tree + table layout. Follow TDD by adding backend integration coverage first, then implement the service/controller logic, then wire the frontend service and view, and finally validate with regression scripts and builds.

**Tech Stack:** Spring Boot, Spring MVC, Spring Data JPA, Vue 3, Element Plus, Vite, Node.js

---

### Task 1: Capture Backend Governance Expectations

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java`

**Step 1: Write the failing test**

Add integration assertions for:

- editing a department name and parent
- editing an account profile and role-bound department
- disabling another account and verifying login is blocked
- enabling the account and verifying login works again
- resetting the password to `123456`
- rejecting self-disable for the current admin

**Step 2: Run test to verify it fails**

Run: `mvn test "-Dtest=AdminOrgIntegrationTest"`
Expected: FAIL because the new routes or service behavior do not exist yet

**Step 3: Keep the failing assertions focused**

Use existing helpers for login and summary reads. Add only the minimum helper methods needed for:

- `updateDepartment`
- `updateAccount`
- `enableAccount`
- `disableAccount`
- `resetPassword`

**Step 4: Commit checkpoint**

Do not commit yet if implementation immediately follows in the same worktree.

### Task 2: Add Backend DTOs and Controller Routes

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/UpdateDepartmentRequest.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/dto/UpdateUserAccountRequest.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/controller/AdminOrgController.java`

**Step 1: Write minimal DTOs**

`UpdateDepartmentRequest` fields:

- `name`
- `parentId`

`UpdateUserAccountRequest` fields:

- `displayName`
- `role`
- `departmentId`
- `title`
- `mobile`

**Step 2: Add controller endpoints**

Add:

- `@PutMapping("/departments/{departmentId}")`
- `@PutMapping("/accounts/{username}")`
- `@PostMapping("/accounts/{username}/enable")`
- `@PostMapping("/accounts/{username}/disable")`
- `@PostMapping("/accounts/{username}/reset-password")`

**Step 3: Re-run focused backend test**

Run: `mvn test "-Dtest=AdminOrgIntegrationTest"`
Expected: FAIL in service layer, but compile succeeds

### Task 3: Implement Backend Governance Logic

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/UserAccountRepository.java` if helper queries are needed
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/DepartmentRepository.java` if helper queries are needed

**Step 1: Implement department update**

Add service logic to:

- load the department
- validate non-empty name
- validate parent exists
- prevent self-parenting
- prevent moving under descendants
- validate sibling-name uniqueness
- save and return updated `DepartmentItem`

**Step 2: Implement account update**

Add service logic to:

- load the account by username
- parse the new role
- validate role/department rules
- keep username and password unchanged
- clear department/title when role becomes patient
- save and return updated `StaffItem`

**Step 3: Implement enable/disable**

Add service logic to:

- toggle `enabled`
- reject disabling the currently logged-in admin
- return updated `StaffItem`

**Step 4: Implement password reset**

Set password to `123456` and return updated `StaffItem`

**Step 5: Run focused backend test**

Run: `mvn test "-Dtest=AdminOrgIntegrationTest"`
Expected: PASS

### Task 4: Add Frontend Governance Service Coverage

**Files:**
- Modify: `frontend/src/services/adminOrg.js`
- Create: `frontend/scripts/test-admin-org-governance.mjs`

**Step 1: Add frontend API methods**

Add methods for:

- `updateAdminDepartment`
- `updateAdminAccount`
- `enableAdminAccount`
- `disableAdminAccount`
- `resetAdminAccountPassword`

**Step 2: Expand model helpers if needed**

Keep `buildAdminOrgModel` as the normalization layer for:

- role labels
- status text
- department path options

**Step 3: Write the failing frontend script**

Assert:

- key governance actions are exposed by the service
- key Chinese copy for governance actions exists in `AdminOrg.vue`

**Step 4: Run script to verify it fails**

Run: `node ./scripts/test-admin-org-governance.mjs`
Expected: FAIL before view/service implementation is complete

### Task 5: Implement Frontend Governance UI

**Files:**
- Modify: `frontend/src/views/admin/AdminOrg.vue`
- Modify: `frontend/src/services/adminOrg.js`

**Step 1: Add operation column and tree edit entry**

Expose:

- edit department
- edit account
- enable/disable account
- reset password

**Step 2: Reuse dialogs for edit mode**

Add mode-aware dialog state so that:

- create account shows password input
- edit account hides password input
- create department and edit department share the same form

**Step 3: Add confirmations**

Use confirm dialogs for:

- disable account
- enable account if desired for consistency
- reset password with `123456` notice

**Step 4: Refresh summary after success**

Re-call `fetchAdminOrgSummary()` after every successful action

**Step 5: Run frontend governance script**

Run: `node ./scripts/test-admin-org-governance.mjs`
Expected: PASS

### Task 6: Run Full Verification

**Files:**
- No code changes expected

**Step 1: Run backend regression**

Run: `mvn test`
Expected: PASS

**Step 2: Run frontend governance script**

Run: `node ./scripts/test-admin-org-governance.mjs`
Expected: PASS

**Step 3: Run production build**

Run: `npm run build`
Expected: PASS

**Step 4: Run bundle verification**

Run: `npm run verify:bundle`
Expected: PASS

**Step 5: Commit**

Recommended:

```bash
git add backend/src/main/java/com/hospital/patientappointments/controller/AdminOrgController.java \
  backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java \
  backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java \
  backend/src/main/java/com/hospital/patientappointments/dto/UpdateDepartmentRequest.java \
  backend/src/main/java/com/hospital/patientappointments/dto/UpdateUserAccountRequest.java \
  backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java \
  frontend/src/services/adminOrg.js \
  frontend/src/views/admin/AdminOrg.vue \
  frontend/scripts/test-admin-org-governance.mjs \
  docs/plans/2026-03-12-admin-org-governance-design.md \
  docs/plans/2026-03-12-admin-org-governance-plan.md
git commit -m "Add admin org governance flows"
```