# Hospital System Remaining Work Assessment

## Current Status

The system has moved beyond a pure demo shell. The patient-side booking and payment flow is connected to real backend data, the doctor-side minimum clinic flow is live, and the admin side already has real dashboard, reports, scheduling, auth overview, and org/account creation.

The remaining gap is no longer "whether the system can run", but "which visible modules are still static, weakly coupled, or only minimally implemented".

## Assessment Summary

### Already Solid Enough For Internal Demo

- Patient booking flow: schedule query, booking, payment, reschedule, cancel, visits, reports, prescriptions
- Doctor minimum clinic flow: queue, start visit, record editing, orders workbench, patient-visible outputs
- Admin core operations: dashboard, operations report, scheduling CRUD, auth account overview, org/account creation
- Basic platform foundations: Spring Boot backend, H2 persistence, token login, role routing

### Still Partial

- Doctor-to-schedule ownership still depends on doctor name text matching
- Doctor schedule page is still static
- Admin billing is still static
- Admin pharmacy is still static
- Admin system configuration is still static
- Org/account management is add-and-view only, without edit/disable/reset-password
- Patient registration is minimum-only and lacks profile completion and auto-login
- Auth is demo-grade, with plain password comparison and no stronger account governance

### Clearly Not Finished

- Real billing catalog, bill lifecycle, invoice flow, reconciliation
- Real pharmacy inventory, stock movement, dispensing flow
- Real system parameter center, dictionary management, ops log, audit log
- Structured doctor diagnosis, order, prescription, and report models
- CI, end-to-end testing, deployment and monitoring

## P0: Must-Fix Before the Next Serious Demo

These items are the highest leverage because they either affect visible trust, break role expectations, or leave high-traffic pages obviously fake.

### 1. Fix doctor-account to schedule binding

**Current issue:** doctor queue ownership is still derived from `displayName -> doctorName` string matching.

**Why it matters:** newly created doctor accounts can log in immediately, but may not see their own queue unless the admin manually keeps display names and schedule doctor names aligned.

**Recommended fix:** add a stable doctor account binding to schedule data, preferably by `doctorUsername` or `doctorAccountId`, and let doctor-side queue lookup rely on that key instead of display text.

**Demo priority:** Must-have

### 2. Make the doctor schedule page real

**Current issue:** [DoctorSchedule.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/doctor/DoctorSchedule.vue) is still static, while admin scheduling is already real.

**Why it matters:** once a doctor logs in, "my schedule" is a natural expectation. A static page weakens the credibility of the doctor-side experience.

**Recommended fix:** let doctors read their real schedules from the admin-managed schedule table. Leave/shift requests can remain postponed, but the schedule list itself should be real.

**Demo priority:** Must-have

### 3. Replace the visible static billing page with a minimum real billing view

**Current issue:** [AdminBilling.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/admin/AdminBilling.vue) is still pure static content.

**Why it matters:** billing is a top-level admin menu entry. If it remains fake, an external walkthrough will immediately expose the unfinished boundary.

**Recommended fix:** implement a minimum real billing center with:

- charge item catalog view
- appointment and visit derived bill list
- payment and refund status summary

No export, reimbursement engine, or invoice printing is needed in this step.

**Demo priority:** Must-have

### 4. Decide how to handle other visible static pages before external walkthroughs

**Current issue:** [AdminPharmacy.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/admin/AdminPharmacy.vue) and [AdminSystem.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/admin/AdminSystem.vue) are still static.

**Why it matters:** even if they are not the main demo path, they are still visible menu entries.

**Recommended fix:** either:

- implement minimum real read-only versions, or
- temporarily hide them from the menu until they are real

**Demo priority:** Must-have

### 5. Run browser-level manual smoke testing

**Current issue:** current confidence is strong at the API, mapping, and build level, but browser click-through acceptance has not been fully performed.

**Why it matters:** once three roles and many linked pages exist, final demo risk often comes from router flow, form interaction, stale state, or visual mismatch rather than unit logic.

**Recommended fix:** run at least one full manual scenario per role:

- patient: register/login -> book -> pay -> see visits, reports, prescriptions
- doctor: login -> queue -> start visit -> save notes -> complete visit
- admin: login -> add department -> add account -> create schedule -> inspect dashboard and reports

**Demo priority:** Must-have

## P1: Important Business Completion Work

These items are not mandatory for the next walkthrough, but they are the most meaningful next layer if the goal is to make the system feel like a fuller hospital platform instead of a strong departmental demo.

### Billing and Revenue

- Turn admin billing into a fuller bill center with charge items, bills, payment records, refund records, and invoice placeholders
- Connect patient payments and admin billing to the same source-of-truth bill model
- Add minimum exception handling for failed or refunded payment records

**Priority:** High

### Pharmacy and Medication Flow

- Build a real drug catalog and stock table
- Add stock-in and stock-out records
- Link doctor prescription notes to a minimum dispensing view

**Priority:** High

### System Configuration and Ops

- Replace static system settings with real parameter storage
- Add dictionary configuration
- Add operation log and audit log read views

**Priority:** High

### Org and Account Governance

- Add account editing
- Add enable or disable
- Add password reset
- Add department editing and safe archiving rules

**Priority:** High

### Patient and Doctor Data Quality

- Add patient profile maintenance
- Add stronger doctor profile fields
- Reduce duplicated text fields across schedule, visit, and account data

**Priority:** Medium

### Structured Clinical Data

- Evolve pure text diagnosis, doctor order, prescription, and report fields into clearer domain structures
- Keep the text-first experience, but prepare the backend for later structured data

**Priority:** Medium

## P2: Engineering and Production Readiness

These items matter once the business scope is stable enough to support longer-term iteration.

### Security Hardening

- Replace plain password comparison with hashed passwords
- Add password policy and reset flow
- Add better token expiration and session governance
- Expand role checks beyond the current minimum boundaries

### Test Coverage and Delivery

- Add frontend component or end-to-end tests beyond mapping scripts
- Add more backend integration coverage for cross-role flows
- Add CI for test, build, and bundle verification

### Deployment and Operations

- Standardize local startup scripts
- Add environment documentation
- Add deployment packaging
- Add monitoring, backup, and restore planning

### Data and Audit Reliability

- Add clearer audit trails for admin changes
- Add safer data migration practices as the schema grows
- Add export or import tools only after the core data model stabilizes

## Demo Must-Have vs Can-Be-Deferred

### Demo Must-Have

- Stable doctor-to-schedule and account binding
- Real doctor schedule page
- Minimum real admin billing page
- Hide or minimally real-ify remaining visible static admin pages
- Browser-level manual acceptance across patient, doctor, and admin roles

### Can Be Deferred

- Rich pharmacy workflow
- Full system parameter center
- Advanced account governance
- Structured clinical data
- CI, deployment, and production operations

## Recommended Next Sequence

1. Fix doctor binding and make doctor schedule real.
2. Build the minimum real admin billing view.
3. Decide whether to hide or minimally implement admin pharmacy and admin system.
4. Run manual end-to-end role walkthroughs and record defects.
5. After the demo boundary is safe, continue with pharmacy, system settings, and account governance.

## Conclusion

The project is already at a stage where the core hospital story can be demonstrated with confidence on patient and doctor flows, and partially on the admin side. The main unfinished work is no longer foundational infrastructure; it is now the cleanup of highly visible static modules, stronger role-to-data binding, and the gradual replacement of minimum demo-grade implementations with real operational features.
