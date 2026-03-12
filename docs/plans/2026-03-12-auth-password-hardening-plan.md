# Auth Password Hardening Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace plaintext password storage and comparison with BCrypt-based hashing while preserving compatibility for existing demo and legacy accounts.

**Architecture:** Add a dedicated password service that can encode, verify, and detect legacy plaintext passwords. Route login, registration, managed account creation, admin password reset, and demo seeding through that service so new writes are hashed and successful legacy logins transparently upgrade stored passwords.

**Tech Stack:** Spring Boot 2.7, Spring Data JPA, `spring-security-crypto`, JUnit 5, MockMvc

---

### Task 1: Add failing password service tests

**Files:**
- Create: `backend/src/test/java/com/hospital/patientappointments/service/PasswordServiceTest.java`
- Create: `backend/src/main/java/com/hospital/patientappointments/service/PasswordService.java`
- Modify: `backend/pom.xml`

**Step 1: Write the failing test**

Create `PasswordServiceTest` with assertions for:

- `encode` returns a value different from the raw password
- `matches` accepts the correct raw password for a hash
- `matches` rejects the wrong raw password
- `isLegacyPlaintext` returns `true` for plain text and `false` for BCrypt hashes

**Step 2: Run test to verify it fails**

Run: `mvn test "-Dtest=PasswordServiceTest"`
Expected: FAIL because `PasswordService` and/or BCrypt dependency do not exist yet

**Step 3: Write minimal implementation**

- Add `spring-security-crypto` to `backend/pom.xml`
- Implement `PasswordService` with:
  - `String encode(String rawPassword)`
  - `boolean matches(String rawPassword, String storedPassword)`
  - `boolean isLegacyPlaintext(String storedPassword)`
  - `boolean shouldUpgrade(String storedPassword)`
- Use `BCryptPasswordEncoder`
- Treat `$2a$`, `$2b$`, `$2y$` as BCrypt markers

**Step 4: Run test to verify it passes**

Run: `mvn test "-Dtest=PasswordServiceTest"`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/pom.xml backend/src/main/java/com/hospital/patientappointments/service/PasswordService.java backend/src/test/java/com/hospital/patientappointments/service/PasswordServiceTest.java
git commit -m "Add password hashing service"
```

### Task 2: Add failing auth compatibility tests

**Files:**
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/AuthIntegrationTest.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AuthService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/repository/UserAccountRepository.java`
- Modify if needed: `backend/src/main/java/com/hospital/patientappointments/model/UserAccount.java`

**Step 1: Write the failing test**

Extend `AuthIntegrationTest` with tests that prove:

- A legacy plaintext account can still log in successfully
- After a successful legacy login, the stored password is no longer plaintext
- A newly registered patient can log in and the stored password is hashed

**Step 2: Run test to verify it fails**

Run: `mvn test "-Dtest=AuthIntegrationTest"`
Expected: FAIL because login still uses plaintext comparison and registration still stores raw passwords

**Step 3: Write minimal implementation**

- Inject `PasswordService` into `AuthService`
- Replace plaintext comparison with `passwordService.matches(...)`
- On successful login, call `passwordService.shouldUpgrade(...)` and persist the hash upgrade when needed
- If repository access is needed for assertions or upgrade helpers, add a focused repository method rather than broad service rewrites

**Step 4: Run test to verify it passes**

Run: `mvn test "-Dtest=AuthIntegrationTest"`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/hospital/patientappointments/service/AuthService.java backend/src/main/java/com/hospital/patientappointments/repository/UserAccountRepository.java backend/src/test/java/com/hospital/patientappointments/integration/AuthIntegrationTest.java
git commit -m "Support hashed passwords in auth flow"
```

### Task 3: Hash passwords on all write paths

**Files:**
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java`
- Modify: `backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java`
- Modify: `backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java`

**Step 1: Write the failing test**

Extend `AdminOrgIntegrationTest` with coverage that proves:

- Newly created managed accounts do not store plaintext passwords
- Reset password writes a hash, not literal `123456`
- The reset password value `123456` can still be used to log in

If needed, add one focused assertion around seeded demo accounts being hashed in a test that boots an empty context.

**Step 2: Run test to verify it fails**

Run: `mvn test "-Dtest=AdminOrgIntegrationTest"`
Expected: FAIL because account creation and reset still persist plaintext passwords

**Step 3: Write minimal implementation**

- Inject `PasswordService` into `AccountProvisioningService`
- Hash passwords in `createManagedAccount(...)`
- Hash passwords in `registerPatient(...)`
- Inject or reuse `PasswordService` in `AdminOrgService.resetPassword(...)`
- Hash demo seed passwords in `DemoDataInitializer`

**Step 4: Run test to verify it passes**

Run: `mvn test "-Dtest=AdminOrgIntegrationTest"`
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java
git commit -m "Hash passwords on account creation and reset"
```

### Task 4: Run focused regression and full backend verification

**Files:**
- Verify only

**Step 1: Run focused password and auth regression**

Run:

```bash
mvn test "-Dtest=PasswordServiceTest,AuthIntegrationTest,AdminOrgIntegrationTest"
```

Expected: PASS with 0 failures

**Step 2: Run full backend suite**

Run:

```bash
mvn test
```

Expected: PASS with 0 failures and 0 errors

**Step 3: Review diff and document outcome**

Run:

```bash
git status --short
git diff --stat
```

Confirm only the intended auth-hardening files changed.

**Step 4: Commit**

```bash
git add backend/pom.xml backend/src/main/java/com/hospital/patientappointments/service/PasswordService.java backend/src/main/java/com/hospital/patientappointments/service/AuthService.java backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java backend/src/test/java/com/hospital/patientappointments/service/PasswordServiceTest.java backend/src/test/java/com/hospital/patientappointments/integration/AuthIntegrationTest.java backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java docs/plans/2026-03-12-auth-password-hardening-design.md docs/plans/2026-03-12-auth-password-hardening-plan.md
git commit -m "Harden password storage and login compatibility"
```