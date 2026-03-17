# Hospital Demo Workspace

This repository contains the demo hospital system backend and frontend.

## Local Verification

- Backend: `cd backend && mvn test`
- Frontend smoke checks: `cd frontend && npm run test:ci`
- Frontend build: `cd frontend && npm run build`
- Frontend bundle budget: `cd frontend && npm run verify:bundle`

## CI Baseline

After this repository is connected to GitHub, `.github/workflows/ci.yml` runs on `push` and `pull_request`.

It validates:

- backend `mvn test`
- frontend `npm run test:ci`
- frontend `npm run build`
- frontend `npm run verify:bundle`

## Connect GitHub Remote

When you create an empty GitHub repository later, connect and push this local repository with:

- `git remote add origin <your-github-repo-url>`
- `git push -u origin main`

## One-Click Local Launch

You can double-click `智慧医院系统.cmd` in the project root to start the local system.

The launcher will:

- reuse running services if `5173` and `8080` are already up
- start the missing frontend/backend service if only one side is down
- open `http://127.0.0.1:5173` in your default browser when the frontend is ready

Launcher logs are written to:

- `backend/launch-backend.log`
- `backend/launch-backend.err.log`
- `frontend/launch-frontend.log`
- `frontend/launch-frontend.err.log`
