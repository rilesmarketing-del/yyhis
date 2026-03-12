# CI Automation Baseline Design

## Background

The repository does not yet include a `.github/workflows` directory. Backend verification already relies on `mvn test`, while frontend verification is built from a set of focused Node scripts plus `npm run build` and `npm run verify:bundle`.

The goal of this phase is not to build a full release pipeline. It is to add a stable GitHub Actions baseline that automatically validates the current demo-critical paths on every `push` and `pull_request`.

## Goals

- Add a GitHub Actions workflow that runs on `push` and `pull_request`
- Keep backend and frontend checks isolated in separate jobs
- Reuse the current backend and frontend verification assets instead of inventing a second test system
- Keep runtime and maintenance cost low enough for a demo-stage project

## Non-Goals

- No deployment automation
- No artifact publishing
- No multi-OS or multi-version build matrix
- No browser E2E framework introduction in this phase
- No attempt to run every frontend script in CI

## Options Considered

### 1. Single workflow with two jobs

Recommended.

Create one workflow file under `.github/workflows/ci.yml` and split execution into `backend` and `frontend` jobs. This keeps the first CI version easy to understand while still making failures easy to localize.

### 2. Separate backend and frontend workflows

This would give more rerun flexibility, but it adds complexity before the repository needs it.

### 3. Matrix-heavy workflow

This improves coverage, but it would slow feedback, complicate maintenance, and add risk for limited practical value at the current stage.

## Recommended Architecture

### Workflow shape

Create a single workflow file:

- `.github/workflows/ci.yml`

Trigger on:

- `push`
- `pull_request`

Use:

- `ubuntu-latest`

Split into two jobs:

- `backend`
- `frontend`

### Backend job

Responsibilities:

- Check out the repository
- Set up Java 15
- Enable Maven dependency caching
- Run backend verification with `mvn test`

This directly matches the current validated backend regression flow.

### Frontend job

Responsibilities:

- Check out the repository
- Set up Node with npm cache
- Run `npm ci`
- Run a curated CI script set covering patient, doctor, and admin critical flows
- Run `npm run build`
- Run `npm run verify:bundle`

The workflow should not call every script under `frontend/scripts`. Instead, it should run a compact, representative subset that protects current demo-critical behavior.

## Frontend CI Scope

Recommended CI smoke script set:

- `node ./scripts/test-patient-registration-handoff.mjs`
- `node ./scripts/test-patient-dashboard.mjs`
- `node ./scripts/test-doctor-orders-workbench.mjs`
- `node ./scripts/test-doctor-clinical-structured-data.mjs`
- `node ./scripts/test-admin-org-governance.mjs`
- `node ./scripts/test-admin-pharmacy-system.mjs`

Then run:

- `npm run build`
- `npm run verify:bundle`

To keep the workflow readable and reusable, add a frontend aggregate script such as `npm run test:ci` in `frontend/package.json`.

## Failure Handling and Timeouts

- Backend and frontend should fail independently in separate jobs
- Each job should have a timeout to prevent hanging runners
- Workflow should fail fast at the job level, but backend and frontend should still be independently visible in the Actions UI
- No artifact upload is needed in this first version

## Caching

- Use `actions/setup-java` built-in Maven caching for backend dependencies
- Use `actions/setup-node` npm caching for frontend dependencies
- Use `npm ci` rather than `npm install` in CI for deterministic installs

## Documentation

Add:

- A design doc for this CI baseline
- An implementation plan
- A short root-level note in `README.md` or a minimal equivalent section if a README already exists, describing what CI validates

If no README currently exists, it is acceptable to add a short one only if it is necessary to explain the new automation entry points.

## Testing Strategy

Before completion:

- Run backend `mvn test`
- Run frontend `npm run test:ci`
- Run frontend `npm run build`
- Run frontend `npm run verify:bundle`
- Validate workflow YAML syntax by careful inspection and, if available locally, a lightweight sanity readback of the generated file contents

## References

- GitHub Docs: Building and testing Java with Maven
- GitHub Docs: Building and testing Node.js
- `actions/setup-java`
- `actions/setup-node`
