# Patient Payments Appointment Integration Design

**Date:** 2026-03-10

## Goal

Turn the patient payments page into a real view over the existing appointment payment flow instead of a static mock table.

## Scope

Included:

- reuse appointment records as the source of truth for payment display
- show unpaid booked appointments in the payments page as payable items
- show paid and refunded appointment payments in payment history
- allow virtual payment from the payments page using the existing payment endpoint
- keep the currently selected demo patient in sync with the displayed payment records

Out of scope:

- standalone bill domain or separate bill table
- examination fees, drug fees, or mixed billing categories
- real invoice generation or download
- real payment gateway integration

## Recommended Approach

Use the current appointment record as the payment-facing record.

- `BOOKED + UNPAID` becomes a pending payment row
- `BOOKED + PAID` becomes a payment history row
- `CANCELLED + REFUNDED` becomes a refund history row

This keeps the demo lightweight and prevents duplicated business data.

## Backend Design

Keep existing endpoints and enrich the appointment response shape for payment display.

Add these response fields to `AppointmentRecordResponse`:

- `fee`: taken from the booked schedule at creation time and persisted in the appointment record
- `paidAt`: null until virtual payment succeeds; set when payment completes and persisted afterward

Persistence rules:

- old persisted records with no `fee` can keep the recorded fee null only if they predate the change, but newly created records must always persist it
- old persisted records with no `paidAt` remain valid
- startup replay still depends only on appointment business status, not payment timestamps

## Frontend Design

The patient payments page becomes a read model over appointment data.

- fetch the current patient's appointments on load
- pending tab shows only `BOOKED + UNPAID`
- history tab shows `BOOKED + PAID` and `CANCELLED + REFUNDED`
- use `serialNumber` as the displayed payment number
- use fixed type text `挂号费`
- use `fee` as amount
- use `paidAt` as payment time when available
- show a status column in payment history so refunded items are distinguishable from paid items

User interaction:

- clicking `立即支付` calls the existing virtual payment endpoint
- after success, refresh data and switch to the history tab
- header pending count always reflects the real unpaid appointment count for the selected demo patient

## Error Handling

- if appointment loading fails, show the backend business or request error message
- if payment fails, preserve the current tab and show the error toast
- empty-state text should clearly indicate there are no payable appointment records yet

## Testing Strategy

Backend:

- payment response includes `fee` and `paidAt`
- `paidAt` persists and survives service reload
- refunded records preserve fee and previous paidAt after reload

Frontend:

- payments page derives unpaid and history lists from real appointment records
- paying from the payments page moves the record from pending to history after refresh
- build and bundle verification still pass

## Recommended Outcome

After this step, the patient-side demo reaches a more convincing business loop:

- appointment page drives the payment state
- payments page reflects that same real state
- users can pay either from appointments or from the dedicated payments center and see consistent results