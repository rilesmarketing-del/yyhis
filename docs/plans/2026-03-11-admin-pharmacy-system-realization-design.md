# Admin Pharmacy and System Minimum Real Views Design

## Goal

Replace the static admin pharmacy and system pages with lightweight, believable read-only pages backed by real project data where available.

## Scope

This step only covers admin-side visibility improvements for walkthroughs.

Included:
- A real admin pharmacy overview derived from visit records with prescription text
- A real admin system overview composed from existing dashboard, operations report, and org summary data
- Clear explanatory copy where the current domain model is intentionally incomplete

Excluded:
- Independent pharmacy inventory domain model
- Drug catalog CRUD
- Parameter editing, dictionary editing, or audit log persistence
- Full configuration center backend

## Recommended Approach

Use a mixed minimum-real strategy.

Why this is the right trade-off now:
- The system already has real visit records with prescription text, which is enough to show pharmacy workload honestly
- The system page can already be built from existing real admin endpoints without adding more backend surface
- This keeps the demo believable without pretending the project already has a complete inventory or configuration subsystem

## Pharmacy Design

### Data Source

Add one small read-only backend endpoint:
- `GET /api/admin/pharmacy/overview`

Build it from `VisitRecord` rows where `prescriptionNote` is not blank.

### UI Shape

Update [AdminPharmacy.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/admin/AdminPharmacy.vue) into a prescription workload overview:

- Hero header with refresh action
- Three summary cards:
  - `处方记录数`
  - `今日处方`
  - `涉及患者`
- Main table:
  - `记录编号`
  - `患者`
  - `科室`
  - `医生`
  - `就诊时间`
  - `接诊状态`
  - `处方摘要`
- A small explanatory card that explicitly says the current page is based on doctor-entered prescription text, and that structured inventory and in/out stock flows are a later phase

### Backend Response Shape

The response should have:

1. `cards`
- `totalPrescriptions`
- `todayPrescriptions`
- `patientCount`

2. `records`
- `id`
- `patientId`
- `patientName`
- `department`
- `doctorName`
- `visitDate`
- `visitTimeSlot`
- `status`
- `prescriptionNote`
- `updatedAt`

Rows should be sorted by `updatedAt` descending so the latest pharmacy activity appears first.

## System Design

### Data Source

Do not add a new backend endpoint for this step.

Build the page by combining:
- `/api/admin/dashboard/summary`
- `/api/admin/reports/operations`
- `/api/admin/org/summary`

### UI Shape

Update [AdminSystem.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/admin/AdminSystem.vue) into a system operations overview:

- Hero header with refresh action
- Four summary cards:
  - `系统账号数`
  - `部门数`
  - `启用中排班`
  - `今日预约`
- A role distribution section based on org summary role stats
- A key operations metrics table based on the operations report
- A system reminders section based on dashboard alerts
- A small note clarifying that editable parameters and dictionary maintenance are still planned, while this page currently focuses on real running signals

## Frontend Service Design

Add dedicated frontend services instead of embedding mapping logic in the views:

- `frontend/src/services/adminPharmacy.js`
  - fetch the pharmacy overview
  - map cards, table rows, and copy for the pharmacy page
- `frontend/src/services/adminSystem.js`
  - fetch the three existing admin data sources in parallel
  - map them into cards, role distribution, metrics, reminders, and empty-state copy

## Testing Design

### Backend

Add an integration test for `/api/admin/pharmacy/overview` that:
- creates at least one visit with prescription text
- creates another visit without prescription text
- verifies only prescription-bearing visits are included
- verifies summary counts
- verifies non-admin access is forbidden

### Frontend

Add mapping tests that:
- verify the pharmacy model formats cards and rows correctly
- verify the system model combines dashboard, reports, and org data into the expected sections
- verify the two pages consume the new real-data services instead of static mock content

## Risks

- Pharmacy still does not represent true stock quantities, batch expiry, or warehouse movement
- System management remains read-only and should not be described as a full config center
- Existing demo text encoding in older files is inconsistent, so new copy should be written carefully and verified in page tests

## Outcome

After this step, the two remaining obviously static admin entries will become honest, data-backed overview pages that are much safer to show during walkthroughs.