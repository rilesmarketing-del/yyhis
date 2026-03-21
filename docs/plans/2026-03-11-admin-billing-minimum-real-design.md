# Admin Billing Minimum Real View Design

## Goal

Replace the static admin billing page with a minimum real, read-only billing center built from the existing appointment payment records.

## Scope

This step only covers admin-side visibility of real billing data.

Included:
- Real admin billing API backed by appointment records
- Billing summary cards for unpaid, paid, and refunded records
- Bill table with patient, department, doctor, visit time, amount, business status, payment status, and payment time
- A small explanatory card clarifying that current billing data comes from挂号预约收费 records

Excluded:
- Independent charge item catalog model
- Bill editing or manual processing
- Invoice flow
- Insurance reconciliation engine
- Refund approval workflow

## Recommended Approach

Use the existing `appointment_records` table as the billing source of truth for this phase.

Why this is the right trade-off now:
- The system already has real unpaid, paid, and refunded appointment records
- The admin billing page only needs to become believable and useful for walkthroughs
- Reusing appointment payment data avoids inventing a premature bill domain model
- The UI can become real without forcing a larger backend redesign this round

## Data Design

Add a new admin billing overview response shaped around two areas:

1. `cards`
- `unpaidCount`
- `paidCount`
- `refundedCount`

2. `bills`
- Reuse appointment-derived billing rows with:
  - `serialNumber`
  - `patientName`
  - `patientId`
  - `department`
  - `doctorName`
  - `date`
  - `timeSlot`
  - `status`
  - `paymentStatus`
  - `fee`
  - `createdAt`
  - `paidAt`

The rows should be sorted by `createdAt` descending so the latest billing activity appears first.

## Backend Design

Add a new read-only admin endpoint:
- `GET /api/admin/billing/overview`

Implementation notes:
- Restrict by the existing `/api/admin/**` auth boundary
- Load appointment records in descending creation order
- Count unpaid, paid, and refunded records
- Map each appointment record into a bill row response
- Return an empty bill list and zero counts safely when there is no data

## Frontend Design

Update [AdminBilling.vue](/C:/Users/89466/Desktop/yy/frontend/src/views/admin/AdminBilling.vue) to match the rest of the admin real-data pages:

- Hero header with refresh action
- Three summary cards:
  - `待支付账单`
  - `已支付账单`
  - `已退款账单`
- Main bill table showing the real billing rows
- Empty state when no bills exist
- A side card explaining that the current billing center is based on appointment registration charges

The page remains read-only for this phase. Buttons like `编辑` or `处理` should be removed because they would be misleading.

## Testing Design

### Backend

Add an integration test that:
- creates one unpaid appointment
- creates one paid appointment
- creates one paid-then-cancelled appointment that becomes refunded
- calls `GET /api/admin/billing/overview`
- verifies the summary counts and returned bill rows
- verifies non-admin users cannot access the endpoint

### Frontend

Add a mapping test that:
- verifies the admin billing model formats the new cards correctly
- verifies bill rows render the correct labels for business and payment status
- verifies the page consumes the new billing model fields instead of static mock arrays

## Risks

- The current billing scope only reflects appointment registration charges, not broader hospital charges
- Refunds are represented through appointment cancellation state, which is acceptable for this minimum phase but not a final finance model
- A future full billing domain should eventually become its own source of truth

## Outcome

After this step, the admin billing page will stop looking fake during walkthroughs and will align with the actual appointment payment lifecycle already present in the system.