# Patient Appointments Reschedule And Virtual Payment Design

**Date:** 2026-03-10

## Goal

Extend the patient appointment MVP with two closely related capabilities:

- virtual payment for booked appointments
- rescheduling for unpaid booked appointments

## Scope

This stage builds on the current lightweight JSON-backed appointment module.

Included:

- add payment status to appointment records
- let patients perform a virtual payment on unpaid booked appointments
- let patients reschedule only unpaid booked appointments
- preserve the new state in the existing JSON persistence file
- surface the new actions and statuses in the patient appointment page

Out of scope:

- real payment gateway integration
- coupon, refund amount, or billing ledger logic
- separate payment page/API domain
- admin reconciliation screens

## Domain Model

The appointment record now has two independent state axes.

### Appointment status

- `BOOKED`
- `CANCELLED`
- `RESCHEDULED`

### Payment status

- `UNPAID`
- `PAID`
- `REFUNDED`

## Business Rules

### Creation

- creating an appointment always results in `BOOKED + UNPAID`

### Virtual payment

- only `BOOKED + UNPAID` appointments can be paid
- payment changes the record to `BOOKED + PAID`
- paid appointments cannot be paid again

### Cancellation

- `BOOKED + UNPAID` cancels to `CANCELLED + UNPAID`
- `BOOKED + PAID` cancels to `CANCELLED + REFUNDED`
- cancelled or rescheduled records cannot be cancelled again
- cancelling still restores one slot to the related schedule

### Rescheduling

- only `BOOKED + UNPAID` appointments can be rescheduled
- rescheduling is implemented as: mark the old record `RESCHEDULED + UNPAID`, restore its slot, then create a new `BOOKED + UNPAID` record on the target schedule
- the patient cannot reschedule to the same schedule
- the patient cannot reschedule into a full schedule
- a paid appointment cannot be rescheduled

## API Design

Keep existing endpoints and add two new mutation endpoints under the same controller.

- `POST /api/patient/appointments/{appointmentId}/pay?patientId=...`
- `POST /api/patient/appointments/{appointmentId}/reschedule`

Reschedule request body:

- `patientId`
- `targetScheduleId`

Reschedule response:

- return the newly created appointment record
- the client refreshes both schedules and appointment list after success

## Persistence Design

The existing JSON persistence stays the single storage layer.

- persist the new `paymentStatus` field in every appointment record
- keep backward compatibility for old records by treating missing payment status as `UNPAID`
- service startup continues to replay only `BOOKED` records when rebuilding remaining schedule slots
- `RESCHEDULED` and `CANCELLED` records do not consume slots after restart

## Frontend Design

The patient appointment page remains the single working surface for this feature.

- show both appointment status and payment status in the "my appointments" table
- unpaid booked records expose `虚拟支付` and `改约`
- paid booked records expose only `取消预约`
- cancelled/rescheduled records show no actions
- clicking `改约` opens a dialog showing the original appointment and the currently available schedules
- after pay, cancel, or reschedule, refresh both appointment list and schedule list

## Error Handling

Backend returns clear business messages for:

- paying an already paid or non-booked appointment
- rescheduling a paid appointment
- rescheduling a cancelled/rescheduled appointment
- rescheduling to the same schedule
- rescheduling to a full or missing schedule

Frontend maps these responses to Element Plus feedback without adding complex recovery flows.

## Testing Strategy

Backend:

- creation defaults to unpaid
- virtual payment changes only payment status
- paid appointments cannot be rescheduled
- unpaid appointments can be rescheduled and move slot usage correctly
- paid cancellation produces refunded payment status
- persisted records restore both appointment status and payment status across service reloads

Frontend:

- build still passes
- bundle verification still passes
- patient appointment page renders the right actions for unpaid vs paid vs terminal records

## Recommended Outcome

After this stage, the patient appointment flow becomes a more realistic demo:

- patients can book, virtually pay, view status, cancel, and reschedule
- payment and reschedule restrictions are explicit and testable
- the entire flow still stays lightweight and file-backed