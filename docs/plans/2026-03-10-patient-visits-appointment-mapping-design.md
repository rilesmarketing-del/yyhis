# Patient Visits Appointment Mapping Design

## Context

The patient visits page is still static, while appointments, payments, and the dashboard already read real patient appointment data. We want visits to become a lightweight downstream view over paid appointments so the patient journey feels more continuous without adding backend APIs or inventing full clinical record data.

## Goals

- Turn paid appointments into visible patient visit records.
- Keep the implementation frontend-only and lightweight.
- Preserve a searchable list and a detail drawer.
- Strengthen the patient-side demo flow from appointment to payment to visit record.

## Non-Goals

- No new backend endpoints.
- No new appointment states such as checked-in or completed.
- No real diagnosis, orders, or doctor-authored clinical notes.
- No changes to reports or prescriptions in this step.

## Data Source

Reuse `fetchMyAppointments(patientId)` and filter to appointments where:

- `status === BOOKED`
- `paymentStatus === PAID`

Only those records will appear in the patient visits page.

## UI Design

### List view

Each paid appointment is mapped to a visit row with the following fields:

- `visitNo`: generated as `VIS-` plus the tail of the appointment serial number
- `dept`: appointment department
- `doctor`: appointment doctor name
- `date`: appointment date and time slot
- `source`: fixed label `预约挂号`
- `status`: fixed label `已登记`

The page keeps a keyword search that matches visit number, department, doctor, or appointment serial number.

### Detail drawer

The drawer will show:

- visit number
- department and doctor
- appointment time
- payment time
- next-step suggestion

Next-step suggestion rules:

- if the appointment date is after today: `请按预约时间到院就诊`
- if the appointment date is today or earlier: `如已完成就诊，可在后续版本查看完整病历`

### Empty state

If the current patient has no paid appointments, show an empty state and provide quick navigation to appointments or payments.

## Implementation Approach

Create a small mapping helper that transforms paid appointments into visit models. Like the dashboard work, keep the mapping logic in a standalone service so a Node regression script can verify it without introducing a new test framework.

`PatientVisits.vue` will load the active patient, fetch appointments, build visit rows from the helper, filter with a keyword, and open a detail drawer from the mapped row.

## Error Handling

If loading fails, show an Element Plus error message and keep the page usable with an empty table and empty-state guidance.

## Verification

- Run a Node regression script for visit mapping.
- Run `npm run build`.
- Run `npm run verify:bundle`.