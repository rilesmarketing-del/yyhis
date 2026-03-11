# P0 Database Auth Scheduling Design

## Context

The current Spring Boot backend still uses in-memory schedule state and JSON-backed appointment persistence. The frontend also relies on frontend-only role switching without backend-backed authentication. The admin scheduling page is static, so patient booking still depends on seeded schedules rather than administrator-managed data.

This P0 phase moves the project from demo-only foundations toward a durable system foundation while keeping Spring Boot as the backend framework.

## Goals

- Replace JSON appointment persistence with database-backed persistence.
- Add a minimal backend-driven authentication flow with fixed demo accounts.
- Make admin scheduling manage real schedule data used by patients.
- Keep existing patient appointment, payment, reschedule, and cancel paths working with minimal API churn.

## Non-Goals

- No full Spring Security rollout in this phase.
- No complete user center or password reset flow.
- No doctor workflow implementation in this phase.
- No deep RBAC model beyond minimal role checks.

## Architecture

### Backend

Keep Spring Boot and introduce:

- H2 database
- Spring Data JPA
- lightweight bearer-token authentication via interceptor
- startup seed initialization for demo accounts and default schedules

The backend becomes the source of truth for:

- user accounts
- login tokens
- schedules
- appointments

### Frontend

Add a small auth layer that:

- logs in with demo accounts
- stores token and current user information locally
- attaches `Authorization: Bearer <token>` to API requests
- routes users into the correct role area after login

Admin scheduling becomes a real CRUD page backed by the new admin schedule APIs.

## Data Model

### UserAccount

Fields:

- `id`
- `username`
- `password`
- `role`
- `displayName`
- `enabled`

Seeded demo accounts:

- patient / patient123
- doctor / doctor123
- admin / admin123

### LoginToken

Fields:

- `token`
- `userId`
- `role`
- `expiresAt`

### ScheduleEntity

Fields:

- `id`
- `doctorName`
- `title`
- `department`
- `date`
- `timeSlot`
- `fee`
- `totalSlots`
- `remainingSlots`
- `enabled`

### AppointmentEntity

Fields remain aligned with the current appointment model:

- patient info
- department / doctor / date / time slot snapshot
- business status
- payment status
- fee
- serial number
- createdAt
- paidAt

## API Design

### Auth APIs

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

### Admin Schedule APIs

- `GET /api/admin/schedules`
- `POST /api/admin/schedules`
- `PUT /api/admin/schedules/{id}`
- `POST /api/admin/schedules/{id}/disable`

### Existing Patient Appointment APIs

Keep current patient appointment paths unchanged as much as possible:

- `GET /api/patient/appointments/schedules`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments/my`
- `POST /api/patient/appointments/{id}/pay`
- `POST /api/patient/appointments/{id}/reschedule`
- `POST /api/patient/appointments/{id}/cancel`

## Authorization Rules

- authenticated `patient` users can access patient appointment APIs only for their own patient identity
- authenticated `admin` users can manage schedules
- authenticated `doctor` users only need login support in this phase
- unauthenticated requests to protected APIs return 401
- wrong-role requests return 403

## Migration Strategy

This phase does not implement a complex migration from JSON into H2. Instead:

- H2 becomes the active persistence mechanism
- startup seeds create demo users and initial schedules if tables are empty
- appointment API response shapes stay as stable as possible to minimize frontend breakage

## Frontend UX

### Login

Add a dedicated login page with three demo account presets or direct username/password input. Successful login stores token and navigates to the role home page.

### Main Layout

Replace free role switching with current-user display and logout action. Role-specific navigation remains driven by the logged-in user.

### Admin Scheduling

Replace the static scheduling table with real schedule listing, create, edit, and disable actions. Patients should immediately see updated schedules from the same data source.

## Testing Strategy

### Backend

Add failing tests first for:

- successful and failed login
- unauthorized access rejection
- admin schedule creation and update
- patient schedule query reading database-backed schedules
- appointment flow continuing to work with JPA persistence

### Frontend

Verify by:

- production build
- bundle-size verification
- basic request path sanity through the new auth service and admin scheduling page wiring

## Risks

- replacing the service internals without preserving response shape could break multiple patient pages at once
- route/auth changes on the frontend can break navigation if login persistence is incomplete
- schedule remaining-slot behavior must stay correct after moving to JPA

## Success Criteria

P0 is complete when:

- login works with backend-issued token
- role-based route entry is enforced by login state
- admin can create and disable schedules through the UI
- patient pages read schedules and appointments from database-backed APIs
- appointment, payment, reschedule, and cancel continue to work end-to-end on Spring Boot + H2