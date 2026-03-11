# Patient Appointments Light Persistence Design

**Date:** 2026-03-10

## Goal

Extend the existing patient appointment MVP with two lightweight improvements:

- let the user switch between a few demo patients on the frontend
- persist appointment records to a local JSON file so records survive backend restarts

## Scope

This iteration stays intentionally small:

- keep the current Vue page and Spring Boot API contract as the main flow
- keep seeded schedules in memory
- persist only appointment records
- keep patient identity as a frontend demo selection, not a real login system

Out of scope:

- database integration
- full patient account management
- admin tools for managing patient data
- persistent schedule configuration

## Frontend Design

The patient appointment page will stop relying on a hard-coded single patient constant.

- add a small demo patient list in a new frontend service/helper module
- store the active patient in `localStorage`
- load schedules and appointments for the selected patient
- when the selected patient changes, refresh the "my appointments" list and keep the schedule query unchanged
- keep the API paths unchanged so only request parameters and payloads continue to carry the patient identity

## Backend Design

Add a lightweight repository component responsible for file persistence.

- file path: `backend/data/appointments.json`
- on application start, create the `data/` directory and file if missing
- read appointment records from disk into memory
- after loading records, replay booked appointments against seeded schedules to restore remaining slots
- after each create/cancel operation, write the full appointment list back to disk

The service remains the main business layer:

- schedule data still comes from `seedSchedules()`
- duplicate booking and cancel rules stay unchanged
- persistence becomes an implementation detail behind the service

## Data Model Notes

Persisted appointment fields:

- `id`
- `scheduleId`
- `patientId`
- `patientName`
- `department`
- `doctorName`
- `date`
- `timeSlot`
- `status`
- `serialNumber`
- `createdAt`

No extra schedule persistence is added. Schedule capacity is restored by replaying booked records on startup.

## Error Handling

- if the persistence file is missing, initialize an empty file and continue
- if the persistence file cannot be read or written, fail fast with a clear runtime exception rather than silently losing data
- if persisted records reference a missing schedule, skip slot replay for that record but keep the record readable

## Testing Strategy

Backend:

- add tests that verify appointments are restored from disk
- verify booked records reduce remaining slots after a fresh service initialization
- verify cancelled records do not reduce remaining slots after reload

Frontend:

- verify selected patient is restored from `localStorage`
- verify appointment queries/bookings use the active patient identity
- keep build verification via `npm run build` and `npm run verify:bundle`

## Recommended Outcome

After this change, the patient appointment module will still feel like a small MVP, but it will behave much more like a usable demo:

- different demo patients can view their own appointment records
- restarting the backend no longer clears all appointment history
