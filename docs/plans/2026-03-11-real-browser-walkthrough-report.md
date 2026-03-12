# Real Browser Walkthrough Report

## Scope

This round executed the approved three-role primary-path walkthrough against the local app using a real Edge browser engine in headless mode.

Covered paths:
- Patient: `login -> dashboard -> appointments -> payments -> visits -> reports -> prescriptions`
- Doctor: `login -> dashboard -> clinic -> records -> orders -> patients -> schedule`
- Admin: `login -> dashboard -> org -> auth -> scheduling -> billing -> pharmacy -> reports -> system`

## Findings

### Fixed in this round

1. Default demo data did not support the main three-role walkthrough.
- Impact: patient, doctor, billing, pharmacy, and reporting pages loaded correctly but showed mostly empty states, which weakened the external demo.
- Root cause: `DemoDataInitializer` only seeded departments, accounts, and schedules. It did not seed appointment or visit records.
- Fix: added a minimum connected demo dataset with paid, unpaid, refunded, waiting, in-progress, and completed records so the main patient, doctor, and admin pages all render believable real data on a fresh startup.

### Browser verification after the fix

Patient path now shows:
- `4` today appointments
- `1` unpaid bill
- non-empty visit list
- real report preview data
- real prescription data

Doctor path now shows:
- `1` waiting patient in queue
- `1` in-progress visit
- `1` completed visit
- non-empty patient list
- non-empty orders workbench

Admin path now shows:
- `4` today appointments
- `2` today visits
- billing summary with unpaid, paid, and refunded rows
- pharmacy summary with real prescription content
- reports and system pages reflecting the seeded operational counts

## Evidence

### Regression test added

- `backend/src/test/java/com/hospital/patientappointments/integration/DemoSeedDataIntegrationTest.java`

This test now verifies that a fresh startup exposes walkthrough-ready demo data for:
- patient appointments and visits
- doctor queue and records
- admin billing and pharmacy summaries

### Real browser outcome

The post-fix browser snapshots confirmed that the previously empty walkthrough pages are now populated with coherent demo data across all three roles.

## Remaining Risks

- This run used a real browser engine in headless mode, not a human-operated visible browser session.
- The current demo dataset is intentionally minimal and centered on one patient-doctor storyline.
- Future work can broaden the seeded dataset if we want more varied departments, patients, or doctors in the walkthrough.

## Outcome

The main demo path is now materially stronger: the app boots into a believable three-role scenario without requiring manual setup before the walkthrough starts.
