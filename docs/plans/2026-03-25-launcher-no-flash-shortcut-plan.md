# Launcher No-Flash Shortcut Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the daily launcher shortcut open the hospital system without the visible `cmd` flash by switching the shortcut target to a hidden-entry script.

**Architecture:** Keep [智慧医院系统.cmd](C:/Users/89466/Desktop/yy/智慧医院系统.cmd) as a fallback troubleshooting entry, but add a new [智慧医院系统.vbs](C:/Users/89466/Desktop/yy/智慧医院系统.vbs) that launches [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1) with a hidden window. Update [create-launcher-shortcut.ps1](C:/Users/89466/Desktop/yy/scripts/create-launcher-shortcut.ps1) so [智慧医院系统.lnk](C:/Users/89466/Desktop/yy/智慧医院系统.lnk) points to the `.vbs` entry while keeping the icon and working directory unchanged.

**Tech Stack:** Windows Script Host, VBScript, Windows PowerShell, COM shortcut automation, PowerShell regression tests

---

### Task 1: Lock the hidden shortcut target with a failing regression

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/scripts/test-launcher-shortcut.ps1`
- Test: `C:/Users/89466/Desktop/yy/scripts/test-launcher-shortcut.ps1`

**Step 1: Write the failing test**

Add assertions that:

- the project root contains a `.vbs` launcher entry
- the shortcut creation path can accept the `.vbs` file as target
- the generated `.lnk` target resolves to the `.vbs` entry instead of the `.cmd`

**Step 2: Run test to verify it fails**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launcher-shortcut.ps1
```

Expected: FAIL because the `.vbs` entry does not exist yet and the shortcut still points to the `.cmd`.

**Step 3: Commit**

Hold commit until all tasks pass.

### Task 2: Add a hidden-entry launcher script

**Files:**
- Create: `C:/Users/89466/Desktop/yy/智慧医院系统.vbs`

**Step 1: Write the minimal implementation**

Create a VBScript entry that:

- locates the project root based on its own file path
- builds the absolute path to `launch-hospital-system.ps1`
- runs `powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "<launcher>"` with hidden window style

**Step 2: Re-run the shortcut regression**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launcher-shortcut.ps1
```

Expected: still FAIL because the shortcut generator has not been switched yet.

**Step 3: Commit**

Hold commit until all tasks pass.

### Task 3: Switch shortcut generation to the hidden entry

**Files:**
- Modify: `C:/Users/89466/Desktop/yy/scripts/create-launcher-shortcut.ps1`
- Test: `C:/Users/89466/Desktop/yy/scripts/test-launcher-shortcut.ps1`

**Step 1: Write the minimal implementation**

Update launcher-path discovery and shortcut creation so:

- both `.cmd` and `.vbs` entry paths are resolved
- shortcut target uses the `.vbs` entry
- stale shortcuts pointing at the old `.cmd` target are replaced

**Step 2: Run the regression**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launcher-shortcut.ps1
```

Expected: PASS.

**Step 3: Regenerate the real shortcut**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\create-launcher-shortcut.ps1
```

Expected: [智慧医院系统.lnk](C:/Users/89466/Desktop/yy/智慧医院系统.lnk) is refreshed to point to the `.vbs` entry.

**Step 4: Commit**

Hold commit until verification completes.

### Task 4: Verify the launcher path end-to-end

**Files:**
- Verify only

**Step 1: Run focused launcher verification**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\yy\scripts\test-launcher-shortcut.ps1
```

Expected: PASS.

**Step 2: Smoke the regenerated shortcut metadata**

Inspect the `.lnk` target through `WScript.Shell` and confirm:

- target path is the `.vbs` entry
- icon path is still the generated `.ico`
- working directory is the project root

**Step 3: Report**

Summarize the root cause, the new hidden-entry chain, and the verification evidence.
