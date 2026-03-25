# Launcher Force Login Entry Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the launcher always land on the login page instead of reopening the last in-app page, while preserving remembered backoffice credentials.

**Architecture:** Keep the launcher health-check URL unchanged, but add a separate browser-entry URL for the launcher that points to `/login?launcher=fresh`. In the frontend router, recognize that query flag, clear the current auth session, and allow the login page to render without redirecting back to a previously authenticated role home.

**Tech Stack:** Windows PowerShell, Vue Router, localStorage-backed auth session, static frontend regression scripts

---

### Task 1: Add failing tests for the forced-login launcher flow

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/scripts/test-launch-hospital-system.ps1`
- Modify: `C:/Users/89466/Desktop/yy/frontend/scripts/test-login-default-entry.mjs`

**Step 1: Write the failing test**

Add assertions that:

- the launcher script exposes a browser-entry URL pointing to `/login?launcher=fresh`
- the frontend router checks for the `launcher` query on `/login`
- the router clears auth session before skipping the auto-redirect

**Step 2: Run tests to verify they fail**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launch-hospital-system.ps1
node C:\Users\89466\Desktop\yy\frontend\scripts\test-login-default-entry.mjs
```

Expected: FAIL because the launcher still opens the root URL and the router does not yet recognize the launcher flag.

### Task 2: Add a launcher-specific browser URL

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/launch-hospital-system.ps1`

**Step 1: Write the minimal implementation**

Add a helper such as `Get-HospitalBrowserUrl` returning:

```text
http://localhost:5173/login?launcher=fresh
```

Keep readiness checks on the base frontend URL if needed, but use the browser URL when calling `Start-Process`.

**Step 2: Re-run the launcher regression**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launch-hospital-system.ps1
```

Expected: launcher assertions pass; frontend router assertion may still fail until Task 3.

### Task 3: Make the login route honor the launcher flag

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/frontend/src/router/index.js`
- Test: `C:/Users/89466/Desktop/yy/frontend/scripts/test-login-default-entry.mjs`

**Step 1: Write the minimal implementation**

On `/login`, detect `query.launcher === "fresh"`, clear the current auth session, and allow the login page to render instead of redirecting to the role home page.

**Step 2: Re-run the focused tests**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launch-hospital-system.ps1
node C:\Users\89466\Desktop\yy\frontend\scripts\test-login-default-entry.mjs
```

Expected: PASS.

### Task 4: Run broader verification

**Files:**
- Verify only

**Step 1: Run frontend regression and build**

Run:

```powershell
cd C:\Users\89466\Desktop\yy\frontend
npm run test:ci
npm run verify:bundle
npm run build
```

Expected: all pass.

**Step 2: Report**

Summarize the root cause, the new launcher browser URL, and the verification evidence.
