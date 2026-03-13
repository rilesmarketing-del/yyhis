# GitHub Actions Online Stabilization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Check the real GitHub Actions workflow runs for the repository and stabilize the baseline CI workflow if the first online run failed.

**Architecture:** Use the existing CI workflow as the baseline, inspect official GitHub run status, and make only the smallest workflow or repository changes required to get the online run green.

**Tech Stack:** GitHub Actions, GitHub repository API/pages, Maven, Node.js, npm

---

### Task 1: Inspect the latest remote CI runs

**Files:**
- No file changes expected

**Step 1: Query the latest workflow runs**

Use an official GitHub source to inspect the latest runs for the repository workflow.

**Step 2: Identify the current state**

Record:

- whether any CI run exists
- latest run status
- latest run conclusion
- failing job or step if present

**Step 3: Stop if already green**

If the latest CI run is already successful, no code change is needed. Document that result and keep the repository unchanged.

### Task 2: Reproduce and isolate any online-only failure

**Files:**
- Possibly inspect but do not modify yet:
  - `C:/Users/89466/Desktop/亿元项目/.github/workflows/ci.yml`
  - `C:/Users/89466/Desktop/亿元项目/frontend/package.json`
  - other files only if directly implicated by the failing step

**Step 1: Map the failure to the current workflow**

Locate the exact failing job and command.

**Step 2: Compare with local behavior**

Run the equivalent local command only for the failing area.

**Step 3: Form a single hypothesis**

State the smallest credible explanation for the online-only failure.

### Task 3: Apply the minimal fix

**Files:**
- Modify only the specific file(s) required by the failing online step

**Step 1: Make the smallest change that tests the hypothesis**

Avoid adding enhancements unrelated to the failure.

**Step 2: Rerun local verification if the changed area has a local equivalent**

Examples:

- workflow command changes -> run the corresponding command locally
- package script changes -> run the updated script locally

### Task 4: Push and confirm online recovery

**Files:**
- No new files expected unless documentation is needed for the fix

**Step 1: Commit the stabilization change**

Use a focused commit message.

**Step 2: Push to `origin/main`**

Trigger a new GitHub Actions run.

**Step 3: Re-check the latest workflow run**

Confirm that the latest CI run succeeds.

### Task 5: Final verification and handoff

**Files:**
- No new files expected

**Step 1: Confirm local repository state**

Run:

- `git status --short`
- `git branch -vv`

**Step 2: Summarize outcome**

Report either:

- online CI is green with no code changes needed, or
- the exact minimal fix applied and the successful follow-up run
