# Patient Appointments Design

**Date:** 2026-03-09

## Goal

Restructure the project under `C:\Users\89466\Desktop\亿元项目` into `frontend/` and `backend/`, then implement a patient appointment booking MVP with a Vue frontend and a Spring Boot backend.

## Current Context

- The existing project is a Vite + Vue 3 + Element Plus frontend living at the repository root.
- There is no backend yet.
- The patient-side screens already exist as UI shells, especially `PatientAppointments.vue`.
- The workspace is not a Git repository, so we can save design and plan documents locally but cannot commit them.
- Java 15 is installed locally.
- Maven is not installed locally, so backend source scaffolding can be completed, but local backend build/start verification may be blocked unless Maven or a wrapper is added later.

## Target Structure

```text
C:\Users\89466\Desktop\亿元项目
├─ frontend
│  ├─ src
│  ├─ scripts
│  ├─ package.json
│  ├─ package-lock.json
│  ├─ index.html
│  └─ vite.config.js
├─ backend
│  ├─ pom.xml
│  └─ src
│     ├─ main
│     │  ├─ java
│     │  └─ resources
│     └─ test
│        └─ java
└─ docs
   └─ plans
```

## Backend Scope

The backend will be a Spring Boot application exposing these endpoints:

- `GET /api/patient/appointments/schedules`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments/my`
- `POST /api/patient/appointments/{id}/cancel`

The first version uses in-memory data only.

## Frontend Scope

The patient appointment page will support:

- filtering schedules by department and date
- viewing doctor schedule cards
- submitting an appointment
- viewing the current patient's appointment records
- cancelling a bookable appointment

## Data Model

### DoctorSchedule
- `id`
- `doctorName`
- `title`
- `department`
- `date`
- `timeSlot`
- `fee`
- `remainingSlots`

### AppointmentRecord
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

## Business Rules

- The same patient cannot book the same schedule twice.
- A schedule with zero remaining slots cannot be booked.
- Only appointments in `BOOKED` state can be cancelled.
- Cancelling an appointment restores one slot to the schedule.

## Error Handling

Backend returns clear JSON error messages for:
- no schedule found
- duplicate booking
- no remaining slots
- invalid cancellation

Frontend maps these responses to user-facing notifications and empty states.

## Verification Plan

Frontend:
- `npm run build`
- `npm run verify:bundle`

Backend if Maven becomes available:
- `mvn test`
- `mvn spring-boot:run`

Manual flow:
- load schedules
- create an appointment
- view the appointment in "my appointments"
- cancel the appointment

## Recommended Implementation Approach

1. Move the current frontend project under `frontend/`.
2. Scaffold a Spring Boot Maven project under `backend/`.
3. Write backend service tests first for booking/cancellation rules.
4. Implement backend controller/service/model layers.
5. Refactor `PatientAppointments.vue` to consume backend APIs.
6. Run all possible verification locally and explicitly report any blocked verification caused by missing Maven.