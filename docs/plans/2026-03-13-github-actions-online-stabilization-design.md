# GitHub Actions Online Stabilization Design

## Background

The repository is now connected to GitHub and the CI baseline workflow has been pushed to the remote repository. Before the repository can be treated as continuously verified, the first real GitHub-hosted workflow runs need to be checked and any online-only failures need to be stabilized.

This phase is about validating the already-designed CI workflow in the actual GitHub Actions environment. It is not about redesigning the pipeline or expanding scope.

## Goals

- Inspect the latest real GitHub Actions runs for the repository
- Identify whether the first online runs succeeded or failed
- If they failed, fix only the minimal online-only blockers
- Re-push and re-check until the baseline workflow runs successfully online

## Non-Goals

- No expansion into release automation
- No artifact uploads, badges, matrix builds, or workflow_dispatch unless required to unblock the baseline
- No product feature work unrelated to CI stabilization
- No manual full-frontend walkthrough request to the user in this phase

## Options Considered

### 1. Observe only

Check run status and stop. This is too passive because it leaves the repository in a half-finished CI state if the online run failed.

### 2. First-run stabilization

Recommended.

Inspect the latest GitHub Actions run, fix only genuine online-environment issues, and re-run until the current baseline works on GitHub-hosted runners.

### 3. Expand the CI pipeline while investigating

This would mix stabilization and enhancement, making failures harder to reason about and increasing the risk of unnecessary changes.

## Recommended Scope

This phase should proceed in this order:

1. Inspect the latest remote workflow run state using official GitHub sources
2. If the latest run failed, identify the failing job and step
3. Compare the failing online step with local verification behavior
4. Apply the smallest fix needed to make the existing workflow run successfully online
5. Push the fix and confirm the next online run succeeds

## Expected Online Failure Categories

The most likely first-run issues are:

- Actions disabled at repository or account level
- Workflow syntax or permissions mismatches on GitHub-hosted runners
- Version differences between local tooling and GitHub-hosted environments
- `npm ci` lockfile or platform issues
- Path, newline, or BOM-related workflow file problems
- Hidden assumptions about local caches or local credentials

## Validation Standard

Successful completion means:

- The latest relevant GitHub Actions run for the CI workflow is successful
- Backend and frontend jobs both complete on GitHub-hosted runners
- Any code or workflow changes made are limited to the blockers revealed by the online environment
- Local verification is rerun if workflow changes affect local commands or package scripts

## Deliverables

- This design document
- A matching implementation plan
- Minimal code or workflow fixes if online failures are found
- A clean commit for the CI stabilization change, if any code is modified

## Notes

This phase is still infrastructure stabilization. Even if GitHub Actions becomes green, that alone is not the signal to start the user on a full manual frontend walkthrough. That recommendation should be made only after a dedicated operator-facing walkthrough pass is prepared.
