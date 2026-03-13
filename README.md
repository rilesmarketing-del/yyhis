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
