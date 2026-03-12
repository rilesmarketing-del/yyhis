# Appointment Database Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Import legacy appointment JSON into the database exactly once at startup when the database is empty, then retire JSON appointment persistence entirely.

**Architecture:** Add a startup migration service that reads `backend/data/appointments.json`, validates legacy records, imports them into `appointment_records` only when the database is empty, and archives the source file after a fully successful import. Remove `AppointmentPersistenceRepository` from the runtime path so appointments are persisted exclusively in the database.

**Tech Stack:** Spring Boot, Spring Data JPA, Jackson, H2, JUnit 5

---

### Task 1: Lock Migration Expectations with Failing Tests

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/service/LegacyAppointmentImportServiceTest.java`
- Modify if needed: `backend/src/test/resources/application.properties`

**Step 1: Write the failing migration tests**

Cover at least:
- imports legacy appointments when database is empty and JSON exists
- skips import when database already contains appointments
- leaves the source file untouched on invalid JSON
- rolls back the whole import when a required field is missing
- rolls back the whole import when duplicate `id` or `serialNumber` appears in the file
- archives the JSON file only after a successful import

**Step 2: Run the focused migration test to verify RED**

Run: `mvn test "-Dtest=LegacyAppointmentImportServiceTest"`
Expected: FAIL because the migration service does not exist yet

**Step 3: Keep test inputs minimal**

Use a temporary directory and temporary JSON files instead of writing into the real `backend/data` folder.

### Task 2: Add the Startup Migration Service

**Files:**
- Create: `backend/src/main/java/com/hospital/patientappointments/service/LegacyAppointmentImportService.java`
- Modify: `backend/src/main/resources/application.properties`
- Modify: `backend/src/test/resources/application.properties`

**Step 1: Add configuration properties**

Introduce properties such as:
- `demo.migration.import-legacy-appointments=true`
- `demo.migration.legacy-appointments-path=./data/appointments.json`

**Step 2: Implement startup trigger**

Make the service run during application startup with strict guards:
- migration toggle enabled
- legacy JSON file exists
- `appointment_records` table is empty

**Step 3: Implement JSON read and validation helpers**

Validate required fields on every record:
- `id`
- `scheduleId`
- `patientId`
- `patientName`
- `department`
- `doctorName`
- `date`
- `timeSlot`
- `status`
- `paymentStatus`
- `fee`
- `serialNumber`
- `createdAt`

Allow:
- `doctorUsername` to be blank
- `paidAt` to be blank

**Step 4: Implement single-transaction import**

Persist all records in one transaction and fail the whole import on any validation or write error.

**Step 5: Implement archive-on-success behavior**

Rename the JSON file to `appointments.imported-<timestamp>.json` only after a successful import.

**Step 6: Run the focused migration test to verify GREEN**

Run: `mvn test "-Dtest=LegacyAppointmentImportServiceTest"`
Expected: PASS

### Task 3: Remove Runtime JSON Persistence Residue

**Files:**
- Delete: `backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java`
- Modify if needed: any files still importing or referencing it

**Step 1: Verify all remaining references**

Run: `rg -n "AppointmentPersistenceRepository|patient-appointments.storage.path|appointments.json" backend/src/main/java`
Expected: only migration-related references remain

**Step 2: Remove the obsolete repository**

Delete the repository or absorb any tiny helper logic into the migration service.

**Step 3: Re-run compile-level verification through tests**

Run: `mvn test "-Dtest=LegacyAppointmentImportServiceTest,PatientAppointmentServiceTest"`
Expected: PASS

### Task 4: Add Startup Integration Coverage

**Files:**
- Create if needed: `backend/src/test/java/com/hospital/patientappointments/integration/LegacyAppointmentImportIntegrationTest.java`
- Modify if needed: `backend/src/test/resources/application.properties`

**Step 1: Write an integration test for startup behavior**

Prove that when the app starts with:
- empty appointment table
- valid legacy JSON file

then imported appointments become readable through existing appointment services or repositories.

**Step 2: Run the focused integration test to verify it fails first**

Run: `mvn test "-Dtest=LegacyAppointmentImportIntegrationTest"`
Expected: FAIL before the integration wiring is complete

**Step 3: Implement the minimal missing wiring**

Only add what the test needs. Avoid refactoring unrelated startup code.

**Step 4: Re-run the focused integration test**

Run: `mvn test "-Dtest=LegacyAppointmentImportIntegrationTest"`
Expected: PASS

### Task 5: Preserve Appointment Business Regression Coverage

**Files:**
- Modify if needed: `backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java`
- Modify if needed: `backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java`

**Step 1: Keep database-based reload assertions intact**

Make sure the existing reload behavior still proves appointments are loaded from the database, not JSON.

**Step 2: Add or adjust one regression assertion if needed**

If the old test names or wording still mention JSON persistence, rename them to reflect database-only behavior.

**Step 3: Run focused business regression tests**

Run: `mvn test "-Dtest=PatientAppointmentServiceTest"`
Expected: PASS

### Task 6: Document Retirement of JSON Persistence

**Files:**
- Create: `docs/plans/2026-03-12-appointment-database-migration-design.md`
- Create: `docs/plans/2026-03-12-appointment-database-migration-plan.md`
- Modify if needed: older plan docs that still describe JSON as the active persistence strategy

**Step 1: Keep the new design and plan docs in the repo**

Ensure these docs describe the migration strategy and retirement of JSON persistence.

**Step 2: Mark older JSON-persistence docs as superseded if needed**

Use a short note instead of rewriting history.

### Task 7: Run Full Verification

**Files:**
- No code changes expected

**Step 1: Run focused migration tests**

Run:
- `mvn test "-Dtest=LegacyAppointmentImportServiceTest"`
- `mvn test "-Dtest=LegacyAppointmentImportIntegrationTest"`

Expected: PASS

**Step 2: Run appointment business regression**

Run: `mvn test "-Dtest=PatientAppointmentServiceTest"`
Expected: PASS

**Step 3: Run full backend suite**

Run: `mvn test`
Expected: PASS

**Step 4: Commit**

Recommended:

```bash
git add backend/src/main/java/com/hospital/patientappointments/service/LegacyAppointmentImportService.java \
  backend/src/main/resources/application.properties \
  backend/src/test/resources/application.properties \
  backend/src/test/java/com/hospital/patientappointments/service/LegacyAppointmentImportServiceTest.java \
  backend/src/test/java/com/hospital/patientappointments/integration/LegacyAppointmentImportIntegrationTest.java \
  backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java \
  docs/plans/2026-03-12-appointment-database-migration-design.md \
  docs/plans/2026-03-12-appointment-database-migration-plan.md

git rm backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java

git commit -m "Migrate legacy appointment JSON into database"
```