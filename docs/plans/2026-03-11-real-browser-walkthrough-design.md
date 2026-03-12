# Real Browser Walkthrough Design

## Goal

Run one real-browser walkthrough across the patient, doctor, and admin experiences using the built-in demo accounts, then fix any defects that would weaken an external demo.

## Scope

This round is focused on the highest-risk demo paths rather than full regression coverage.

Included:
- Start the current frontend and backend locally
- Log in with the built-in demo accounts `patient / doctor / admin`
- Click through the primary business paths for each role in a real browser
- Record findings with severity and reproduction context
- Fix walkthrough defects that are practical to address in this round
- Re-run smoke verification after any fixes

Excluded:
- Full exploratory coverage of every page state and edge case
- Patient self-registration as a primary acceptance path
- Playwright or Cypress setup
- Visual redesign work unrelated to walkthrough findings
- Long-tail product improvements that do not affect the next demo

## Recommended Approach

Use a primary-path browser walkthrough with the existing demo accounts as the source of truth for acceptance in this round.

Why this is the right trade-off now:
- It validates the exact flow a demo audience is most likely to see
- It catches integration issues that script-only checks can miss, such as navigation friction, stale copy, and state sync gaps
- It keeps the effort focused on demo readiness instead of broad QA expansion
- It preserves time for fixing issues immediately after they are observed

## Walkthrough Design

### Accounts

Use the seeded demo accounts from the current app:
- Patient: `patient / patient123`
- Doctor: `doctor / doctor123`
- Admin: `admin / admin123`

### Patient Path

Walk through:
- `login`
- `patient dashboard`
- `patient appointments`
- `patient payments`
- `patient visits`
- `patient reports`
- `patient prescriptions`

Checkpoints:
- the user can enter each page without broken navigation
- appointment, payment, visit, and report information use believable business data
- labels do not expose raw internal identifiers when user-facing identifiers exist
- cross-page state remains consistent after navigating between billing and visit/report pages

### Doctor Path

Walk through:
- `login`
- `doctor dashboard`
- `doctor clinic`
- `start or inspect an existing visit`
- `doctor records`
- `doctor orders`
- `doctor patients`
- `doctor schedule`

Checkpoints:
- the doctor only sees their own queue and schedule
- visit details and linked patient data remain coherent across clinic, records, and workbench pages
- schedule and patient summaries read as real workflow data rather than placeholders

### Admin Path

Walk through:
- `login`
- `admin dashboard`
- `admin org`
- `admin auth`
- `admin scheduling`
- `admin billing`
- `admin pharmacy`
- `admin reports`
- `admin system`

Checkpoints:
- every admin page loads from real data without obviously fake tables
- scheduling, billing, pharmacy, and system pages present coherent summary and detail information
- admin navigation reflects the latest implemented features and copy

## Finding Classification

Use three severity levels while executing the walkthrough:

1. `Demo blocker`
- prevents reaching a key page or completing a key action
- shows obviously broken data or crashes

2. `Visible defect`
- the flow works but the page shows misleading copy, stale labels, inconsistent data, or obvious UI issues

3. `Follow-up improvement`
- polish or depth issues that do not threaten the next demo

## Execution Evidence

Capture the outcome of each step with:
- whether the page could be entered
- whether the key information looked real and coherent
- whether any cross-page state mismatches appeared

Record the findings in a fresh walkthrough report under `docs/plans`.

## Testing Design

If the browser walkthrough uncovers defects:
- add or extend focused regression tests around the affected mapping or API behavior first
- confirm the test fails before the fix when practical
- implement the minimum fix
- re-run the targeted checks plus the broader smoke and build verification already used in this repo

If no defects are found:
- still produce a walkthrough report and re-run the core verification commands as evidence

## Outcome

After this round, we should have a browser-validated picture of whether the current three-role demo is presentable, plus any fixes and regression coverage needed to keep it stable.
