# Launcher Shortcut Icon Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a branded Windows shortcut with a custom hospital icon for the launcher while keeping the existing `.cmd` file as the real startup entry.

**Architecture:** Keep [鏅烘収鍖婚櫌绯荤粺.cmd](/C:/Users/89466/Desktop/浜垮厓椤圭洰/鏅烘収鍖婚櫌绯荤粺.cmd) unchanged as the executable entry point. Add reusable asset-generation and shortcut-generation helpers that produce a local [鏅烘収鍖婚櫌绯荤粺.lnk](/C:/Users/89466/Desktop/浜垮厓椤圭洰/鏅烘収鍖婚櫌绯荤粺.lnk) pointing to the `.cmd`, with its icon bound to a new project-owned `.ico`.

**Tech Stack:** Windows PowerShell, WScript.Shell COM, SVG asset, ICO generation, Git-tracked scripts

---

### Task 1: Lock shortcut behavior with a failing regression

**Files:**
- Create: `C:/Users/89466/Desktop/浜垮厓椤圭洰/scripts/test-launcher-shortcut.ps1`
- Test: `C:/Users/89466/Desktop/浜垮厓椤圭洰/scripts/test-launcher-shortcut.ps1`

**Step 1: Write the failing test**

Add assertions that:

- the shortcut generation script exposes helper functions
- icon generation can create a non-empty `.ico`
- shortcut creation writes a `.lnk`
- shortcut target resolves to [鏅烘収鍖婚櫌绯荤粺.cmd](/C:/Users/89466/Desktop/浜垮厓椤圭洰/鏅烘収鍖婚櫌绯荤粺.cmd)
- shortcut icon location points to the generated `.ico`

**Step 2: Run test to verify it fails**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\浜垮厓椤圭洰\scripts\test-launcher-shortcut.ps1
```

Expected: fail because the generation script does not exist yet.

**Step 3: Commit**

Hold commit until all tasks pass.

### Task 2: Add the icon asset source and generation script

**Files:**
- Create: `C:/Users/89466/Desktop/浜垮厓椤圭洰/assets/icons/hospital-launcher.svg`
- Create: `C:/Users/89466/Desktop/浜垮厓椤圭洰/scripts/create-launcher-shortcut.ps1`

**Step 1: Write the minimal implementation**

Create:

- a clean SVG source asset with the approved 鈥滃尰鐤椾腑鏋㈤鈥?- a PowerShell script that:
  - resolves project paths
  - generates the `.ico`
  - creates or refreshes the `.lnk`
  - prints the output paths

**Step 2: Run the shortcut regression**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\浜垮厓椤圭洰\scripts\test-launcher-shortcut.ps1
```

Expected: pass.

**Step 3: Generate the real local shortcut**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\浜垮厓椤圭洰\scripts\create-launcher-shortcut.ps1
```

Expected: local `.ico` and `.lnk` are present in the project.

**Step 4: Commit**

Use:

```powershell
git -C C:\Users\89466\Desktop\浜垮厓椤圭洰 add C:\Users\89466\Desktop\浜垮厓椤圭洰\assets\icons\hospital-launcher.svg C:\Users\89466\Desktop\浜垮厓椤圭洰\assets\icons\hospital-launcher.ico C:\Users\89466\Desktop\浜垮厓椤圭洰\scripts\create-launcher-shortcut.ps1 C:\Users\89466\Desktop\浜垮厓椤圭洰\scripts\test-launcher-shortcut.ps1 C:\Users\89466\Desktop\浜垮厓椤圭洰\docs\plans\2026-03-18-launcher-shortcut-icon-design.md C:\Users\89466\Desktop\浜垮厓椤圭洰\docs\plans\2026-03-18-launcher-shortcut-icon-plan.md
git -C C:\Users\89466\Desktop\浜垮厓椤圭洰 commit -m "Add branded launcher shortcut icon"
```

### Task 3: Verify before reporting success

**Files:**
- Verify only

**Step 1: Run verification**

Run:

```powershell
powershell -NoLogo -ExecutionPolicy Bypass -File C:\Users\89466\Desktop\浜垮厓椤圭洰\scripts\test-launcher-shortcut.ps1
```

Expected: pass.

**Step 2: Report**

Summarize:

- where the icon asset lives
- where the shortcut lives
- that the shortcut points to [鏅烘収鍖婚櫌绯荤粺.cmd](/C:/Users/89466/Desktop/浜垮厓椤圭洰/鏅烘収鍖婚櫌绯荤粺.cmd)

