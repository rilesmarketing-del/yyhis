# Launcher Hidden Entry Fix Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the double-click launcher start the hospital system without leaving a visible `cmd` window while reliably opening the frontend page.

**Architecture:** Keep [智慧医院系统.cmd](C:/Users/89466/Desktop/亿元项目/智慧医院系统.cmd) as the user-facing entry, but convert it into a self-hiding trampoline that hands control to [launch-hospital-system.ps1](C:/Users/89466/Desktop/亿元项目/launch-hospital-system.ps1). In the PowerShell launcher, use the stable default `npm run dev` frontend command, align the health check URL with the actual Vite-ready address `http://localhost:5173`, and treat `404` as a valid “frontend is up” response.

**Tech Stack:** Windows CMD, Windows PowerShell, Maven, npm, Vite, custom PowerShell regression script

---

### Task 1: Lock the launcher behavior with tests

**Files:**
- Modify: `C:/Users/89466/Desktop/亿元项目/scripts/test-launch-hospital-system.ps1`
- Test: `C:/Users/89466/Desktop/亿元项目/scripts/test-launch-hospital-system.ps1`

**Step 1: Write the failing test**

Add assertions that:

- the entry file contains a hidden relaunch marker variable
- the entry file uses a hidden PowerShell start path before calling `launch-hospital-system.ps1`
- the launcher script exposes a frontend command string using the default Vite dev startup form
- the readiness helper treats `404` as “frontend is up”

**Step 2: Run test to verify it fails**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected: fail because the current `.cmd` entry does not hide itself and the launcher does not yet expose the corrected readiness behavior.

**Step 3: Write minimal implementation**

Do not implement here; proceed to Tasks 2 and 3 after the red test is confirmed.

**Step 4: Run test to verify it still fails correctly**

Run the same command and confirm the failure points at the missing hidden entry / frontend command behavior rather than a syntax error.

**Step 5: Commit**

Hold commit until all tasks pass.

### Task 2: Convert the `.cmd` entry into a hidden trampoline

**Files:**
- Modify: `C:/Users/89466/Desktop/亿元项目/智慧医院系统.cmd`

**Step 1: Write the minimal implementation**

Update the entry file to:

- set a guard variable such as `HOSPITAL_LAUNCHER_HIDDEN`
- on first launch, use PowerShell with a hidden window to relaunch the same `.cmd`
- exit the visible instance immediately
- on the hidden pass, call `launch-hospital-system.ps1`

**Step 2: Run test to verify the entry assertions pass**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected: the hidden-entry assertions pass; the frontend-command assertion may still fail until Task 3 is done.

**Step 3: Commit**

Hold commit until all tasks pass.

### Task 3: Fix the health check path and keep startup behavior aligned

**Files:**
- Modify: `C:/Users/89466/Desktop/亿元项目/launch-hospital-system.ps1`
- Test: `C:/Users/89466/Desktop/亿元项目/scripts/test-launch-hospital-system.ps1`

**Step 1: Write the minimal implementation**

Refactor the launcher so:

- the frontend URL helper returns `http://localhost:5173`
- the frontend startup helper uses the stable `npm run dev` invocation
- the readiness helper treats `2xx-4xx` as “frontend is ready”

**Step 2: Run the launcher regression test**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected: pass.

**Step 3: Smoke the launcher behavior**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1
```

Expected: frontend and backend start successfully, frontend logs show Vite on `127.0.0.1:5173`, and the browser target URL responds.

**Step 4: Commit**

Use:

```powershell
git -C C:\Users\89466\Desktop\亿元项目 add C:\Users\89466\Desktop\亿元项目\智慧医院系统.cmd C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1 C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1 C:\Users\89466\Desktop\亿元项目\docs\plans\2026-03-18-launcher-hidden-entry-fix-design.md C:\Users\89466\Desktop\亿元项目\docs\plans\2026-03-18-launcher-hidden-entry-fix-plan.md
git -C C:\Users\89466\Desktop\亿元项目 commit -m "Fix hidden launcher entry behavior"
```

### Task 4: Run verification before reporting success

**Files:**
- Verify only

**Step 1: Run regression and build verification**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
cd C:\Users\89466\Desktop\亿元项目\frontend
npm run test:ci
npm run build
npm run verify:bundle
```

Expected: all commands exit successfully.

**Step 2: Report**

Summarize the root cause, the launcher behavior change, and the exact verification evidence.
