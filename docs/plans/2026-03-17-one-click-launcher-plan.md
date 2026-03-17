# One-Click Launcher Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a double-click Windows launcher that starts missing frontend/backend services and opens the hospital system homepage automatically.

**Architecture:** Use a small root `.cmd` file as the double-click entry point and delegate all logic to a PowerShell launcher. The PowerShell script will detect running ports, start only missing services, wait for the frontend to become reachable, and then open the browser. A lightweight PowerShell regression script will verify helper behavior before implementation changes are made.

**Tech Stack:** Windows CMD, PowerShell, Maven, npm, Vite, Spring Boot

---

### Task 1: Add the launcher regression script

**Files:**
- Create: `C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1`
- Test: `C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1`

**Step 1: Write the failing test**

Create a PowerShell test script that:

- dot-sources `launch-hospital-system.ps1`
- asserts helper functions exist
- asserts the frontend/backend log paths resolve under the expected directories
- asserts the browser URL is `http://127.0.0.1:5173`

**Step 2: Run test to verify it fails**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- FAIL because `launch-hospital-system.ps1` does not yet exist

**Step 3: Commit**

Do not commit yet. Keep iterating until the implementation exists.

### Task 2: Create the PowerShell launcher

**Files:**
- Create: `C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1`
- Test: `C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1`

**Step 1: Write minimal implementation**

Implement the script with:

- a script-root based project path
- helper functions for:
  - checking whether a TCP port is listening
  - returning frontend/backend log file paths
  - starting frontend in the background if needed
  - starting backend in the background if needed
  - waiting for frontend readiness
  - opening the browser
- a guard so tests can dot-source the file without auto-running the launcher

**Step 2: Run test to verify it passes**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- PASS with helper assertions satisfied

**Step 3: Refactor**

Clean up naming and keep the launcher logic readable without changing behavior.

### Task 3: Add the double-click entry file

**Files:**
- Create: `C:\Users\89466\Desktop\亿元项目\智慧医院系统.cmd`
- Modify: `C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1`

**Step 1: Write the failing test**

Extend `test-launch-hospital-system.ps1` to assert:

- `智慧医院系统.cmd` exists
- it invokes `launch-hospital-system.ps1`

**Step 2: Run test to verify it fails**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- FAIL because the `.cmd` entry file is not present yet

**Step 3: Write minimal implementation**

Create `智慧医院系统.cmd` that:

- resolves its own directory
- calls `powershell.exe -ExecutionPolicy Bypass -File "%~dp0launch-hospital-system.ps1"`

**Step 4: Run test to verify it passes**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- PASS

### Task 4: Validate launcher behavior locally

**Files:**
- Modify: `C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1`
- Test: `C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1`

**Step 1: Write the failing test**

Extend the regression script with a small smoke assertion that:

- calls the launcher in test mode and verifies it chooses not to restart already-running ports

**Step 2: Run test to verify it fails**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- FAIL until the launcher exposes test-mode behavior or injectable port status

**Step 3: Write minimal implementation**

Add an injectable port-state layer or test-mode override so the launcher logic can be exercised without spawning real services inside the test.

**Step 4: Run test to verify it passes**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- PASS

### Task 5: Document how to use the launcher

**Files:**
- Modify: `C:\Users\89466\Desktop\亿元项目\README.md`

**Step 1: Write the failing test**

No automated test required for README-only changes.

**Step 2: Write minimal implementation**

Add a short section describing:

- double-click `智慧医院系统.cmd`
- what it starts
- which URL it opens
- where logs are written

**Step 3: Verify manually**

Read the README section and confirm it matches the implemented behavior.

### Task 6: Run final verification and commit

**Files:**
- Modify: `C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1`
- Modify: `C:\Users\89466\Desktop\亿元项目\智慧医院系统.cmd`
- Modify: `C:\Users\89466\Desktop\亿元项目\README.md`
- Modify: `C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1`

**Step 1: Run targeted verification**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1
```

Expected:

- PASS

**Step 2: Run broader verification**

Run:

```powershell
cd C:\Users\89466\Desktop\亿元项目\frontend
npm run test:ci
npm run build
npm run verify:bundle
```

Expected:

- PASS

**Step 3: Commit**

```bash
git add C:\Users\89466\Desktop\亿元项目\智慧医院系统.cmd C:\Users\89466\Desktop\亿元项目\launch-hospital-system.ps1 C:\Users\89466\Desktop\亿元项目\scripts\test-launch-hospital-system.ps1 C:\Users\89466\Desktop\亿元项目\README.md C:\Users\89466\Desktop\亿元项目\docs\plans\2026-03-17-one-click-launcher-design.md C:\Users\89466\Desktop\亿元项目\docs\plans\2026-03-17-one-click-launcher-plan.md
git commit -m "Add one-click local launcher"
```
