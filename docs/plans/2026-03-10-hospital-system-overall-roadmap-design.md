# Hospital System Overall Roadmap Design

## Current State

The project has been split into `frontend/` and `backend/` and already supports a patient-side demo chain around appointments.

Implemented so far:

- appointment schedule query
- appointment creation
- my appointments
- cancel appointment
- lightweight JSON persistence for appointment data
- demo patient switching
- virtual payment
- reschedule
- payment center linked to real appointment data
- patient dashboard linked to real appointment data
- patient visits mapped from paid appointments
- patient reports mapped from paid and due appointments

The current product is a strong patient-side demo, but the overall hospital system is still missing formal identity, operational back-office data management, doctor-side workflow closure, and production-grade engineering foundations.

## Roadmap Goals

- Turn the patient-side demo chain into a more complete patient business flow.
- Connect doctor-side clinical operations to the patient-side results already being displayed.
- Make admin-side pages control real schedules, people, billing rules, and reporting.
- Replace demo persistence and role assumptions with production-ready foundations.

## Product Strategy

The recommended strategy is to continue in four parallel tracks, but execute them by priority rather than trying to make every role complete at once.

### Track A: Platform foundations

This track removes demo-only technical constraints:

- real authentication and authorization
- database persistence
- environment configuration and startup scripts
- consistent API error model and validation
- test strategy for frontend and backend

This track is the base for every later module.

### Track B: Patient-side business completion

This track deepens the patient journey already implemented:

- better appointment state progression
- richer payment and invoice handling
- stronger visit and report semantics
- prescription and medication pickup linkage
- patient reminders and notifications

This provides the clearest user-facing value and the best demo continuity.

### Track C: Doctor-side workflow closure

This track lets doctor actions generate the patient data now simulated on the frontend:

- queue and clinic workflow
- patient lookup
- medical record editing
- orders and prescriptions
- visit completion and report publishing

Without this track, patient-side records remain derived placeholders.

### Track D: Admin-side operating system

This track turns admin pages into real control surfaces:

- department and staff management
- scheduling and slot management
- billing catalog and reimbursement policies
- pharmacy inventory and dispense operations
- reporting and audit logs

This track enables sustainable real-world operation instead of a standalone demo.

## Priority Model

### P0: Make the current demo reliable and extensible

Focus on the smallest set of work that removes architectural fragility:

- replace local JSON persistence with database persistence
- add login or at least role-based mock authentication backed by backend sessions or tokens
- make schedules manageable from the admin side instead of hardcoded seed data
- keep appointment, payment, visits, and reports consistent under one source of truth

### P1: Close the cross-role operational loop

After P0, connect patient, doctor, and admin responsibilities:

- doctor clinic and medical records create real downstream patient data
- admin scheduling controls what patients can book
- billing rules generate real payment items
- refunds and cancellations follow explicit policies

### P2: Improve completeness, observability, and delivery quality

This is the stage for reporting depth, performance, deployment, and operational excellence:

- broader reporting and export capabilities
- audit trails and operation logs
- CI/CD and deployment packaging
- monitoring, backup, and data migration
- broader automated testing

## Recommended Sequencing

### Phase 1: Foundation hardening

1. Introduce database persistence for appointments, schedules, and patient demo identities.
2. Add a minimal authentication model.
3. Normalize backend DTOs, validation, and error handling.

### Phase 2: Patient-side business hardening

1. Make payment items and invoice records more explicit.
2. Replace derived patient visits and reports with backend-generated domain records where appropriate.
3. Connect prescriptions and medication pickup.

### Phase 3: Doctor-side real workflow

1. Make the doctor dashboard and clinic queue data-driven.
2. Let doctors create or complete visits.
3. Let doctor actions generate patient-visible reports, prescriptions, and history.

### Phase 4: Admin-side operating controls

1. Make organization, staff, and schedules editable.
2. Add billing rule administration.
3. Add reporting and audit capability.

### Phase 5: Engineering and delivery

1. Add automated frontend tests.
2. Add backend integration tests.
3. Add startup scripts, environment docs, and deployment packaging.
4. Add CI checks and release discipline.

## Risks

- If database persistence is postponed too long, every new module will build on fragile demo storage.
- If doctor-side workflow is postponed too long, patient-side visits and reports will stay synthetic.
- If admin scheduling stays static, patient booking logic will eventually diverge from operations needs.
- If frontend testing is not introduced, the growing patient-side flow will become expensive to maintain.

## Success Criteria

The next meaningful milestone for the overall system is:

- admin can manage schedules
- patient can book and pay against admin-managed slots
- doctor can see the patient in clinic and complete the visit
- patient can later see real visit history, reports, and prescriptions
- core data survives restart and is backed by a real database