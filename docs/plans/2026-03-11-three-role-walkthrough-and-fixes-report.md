# Three-Role Walkthrough And Fixes Report

## Scope

This round used route inspection plus scriptable smoke verification in the current terminal-only environment.

Covered walkthrough paths:
- Patient: dashboard -> appointments -> payments -> visits -> reports -> prescriptions
- Doctor: dashboard -> clinic -> records -> orders -> patients -> schedule
- Admin: dashboard -> org -> auth -> scheduling -> billing -> reports -> pharmacy -> system

## Findings

### Fixed in this round

1. Patient dashboard quick action still described the reports module as a demo-only entry.
- Impact: users would see outdated copy even though the reports page already uses real data.
- Fix: updated `patientDashboard.js` so the reports quick action now points users to real reports and follow-up advice.

2. Patient visit details surfaced internal `appointmentId` values instead of user-facing appointment serial numbers when real visit records existed.
- Impact: walkthroughs would reveal backend IDs in the patient UI.
- Fix: `patientVisits.js` now resolves matching appointment serial numbers from the appointment list and also carries over paid time when available.

3. Patient report previews surfaced internal `appointmentId` values instead of user-facing appointment serial numbers when real visit reports existed.
- Impact: the report drawer would show backend IDs instead of business identifiers.
- Fix: `patientReports.js` now resolves matching appointment serial numbers from the appointment list.

### Remaining limits

- This was not a true browser automation run because the project currently has no Playwright/Cypress-style setup in the workspace.
- A final manual click-through in a real browser is still recommended before any external demo.

## Evidence

Targeted patient smoke scripts were updated to lock the fixes:
- `frontend/scripts/test-patient-dashboard.mjs`
- `frontend/scripts/test-patient-visits.mjs`
- `frontend/scripts/test-patient-reports.mjs`

These now verify:
- patient dashboard report entry wording is current
- visit/report rows resolve business serial numbers instead of raw internal IDs