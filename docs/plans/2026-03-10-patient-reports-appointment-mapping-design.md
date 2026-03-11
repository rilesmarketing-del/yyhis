# Patient Reports Appointment Mapping Design

## Context

The patient reports page is still static, while appointments, payments, dashboard, and visits already use real appointment-derived data. We want reports to become the next lightweight downstream view so the patient-side demo chain can continue from paid appointments into report browsing without adding backend APIs.

## Goals

- Turn eligible appointments into report placeholder rows.
- Keep the implementation frontend-only and lightweight.
- Preserve search, list, and preview dialog interactions.
- Make the patient journey feel continuous from appointment to payment to visit to reports.

## Non-Goals

- No new backend endpoints.
- No new appointment or visit states.
- No integration with real LIS, PACS, or doctor-authored report systems.
- No changes to prescriptions in this step.

## Data Source

Reuse `fetchMyAppointments(patientId)` and keep only appointments where:

- `status === BOOKED`
- `paymentStatus === PAID`
- `date <= today`

Only those records will appear as report placeholders.

## UI Design

### List view

Each eligible appointment is mapped to a report row with:

- `reportNo`: `REP-` plus the tail of the appointment serial number
- `item`: derived from department using a small mapping, falling back to `门诊随访报告`
- `date`: prefer `paidAt`, otherwise fall back to appointment date
- `result`: default `正常`; `心内科` and `呼吸内科` become `待复核`
- `summary`: short sentence derived from department, doctor, and appointment date
- `advice`: short next-step recommendation derived from appointment date
- `serialNumber`: original appointment number for tracing

### Search

Keyword search will match report number, item, department, doctor, and appointment serial number.

### Preview dialog

The report preview keeps the existing structure and adds `关联预约单号`.

### Empty state

If the patient has no eligible appointments, show an empty state with navigation to:

- `/patient/visits`
- `/patient/appointments`

## Implementation Approach

Create a standalone mapping helper that transforms eligible appointments into patient report rows. Like the dashboard and visits work, keep the mapping in a service plus a Node regression script so we can verify behavior without adding a frontend test runner.

`PatientReports.vue` will load the active patient, fetch appointments, call the mapping helper, render filtered rows, and open the preview dialog from mapped data.

## Error Handling

If loading fails, show an Element Plus error message and keep the page usable with an empty list and empty-state navigation.

## Verification

- Run a Node regression script for report mapping.
- Run `npm run build`.
- Run `npm run verify:bundle`.