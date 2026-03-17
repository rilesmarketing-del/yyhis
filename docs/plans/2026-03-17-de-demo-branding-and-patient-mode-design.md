# De-demo Branding And Patient Mode Isolation Design

**Date:** 2026-03-17

## Goal

Remove explicit demo-oriented wording from the primary frontend entry surfaces and add a browser-local patient mode that hides doctor/admin access across the whole frontend until a real administrator authenticates to unlock it.

## Scope

In scope:
- Login page branding and access presentation
- Shared shell account display and top brand copy
- Browser-local patient mode state and unlock flow
- Router-level access blocking for doctor/admin routes while patient mode is enabled
- Focused frontend regression coverage for the above behavior

Out of scope:
- Backend-global kiosk mode
- A separate patient-mode password model
- Full-site copy rewrite for every business page
- New user roles or auth primitives beyond existing admin login

## Product Decisions

### 1. Remove demo wording from primary entry surfaces

The frontend should present itself as a production-facing hospital system rather than a demo. The login page and shared shell are the highest-visibility locations, so they become the primary copy cleanup targets.

Changes:
- Remove wording such as “演示”, “体验”, and “演示工作台” from the login page and shared shell
- Replace “Hospital Demo” and “演示工作台” with neutral product language
- Keep the existing system name “智慧医院业务系统”
- Rephrase login-page explanation cards so they describe real operational usage rather than demo usage

### 2. Simplify account identity display in the shell

The current top-right account display over-explains the current role by showing role-end wording beside the username. The shell should display identity more naturally.

Changes:
- Keep display name as the primary identity label
- Keep username as the secondary label
- Remove “当前账号” chip wording that reads like a demo scaffold
- Remove “医生端 / 管理端 / 患者端” labeling from the account identity area
- Preserve route/breadcrumb context elsewhere so navigation remains understandable

This specifically satisfies the requirement that doctors should not see their account labeled as belonging to a particular “end”.

### 3. Add browser-local patient mode

Patient mode is a kiosk/front-desk style viewing mode stored in the current browser only. It is not global server configuration.

When patient mode is enabled:
- The login page shows only patient-facing login and registration actions
- Doctor/admin quick-entry and related marketing copy are hidden
- Doctor/admin routes are blocked and redirected back to `/login`
- Existing doctor/admin sessions are cleared when the mode is entered or when blocked routes are encountered

When patient mode is disabled:
- The system returns to the normal multi-role access presentation

### 4. Unlock patient mode with real admin credentials

Disabling patient mode requires real administrator authentication. No separate unlock password is introduced.

Flow:
- The login page exposes an “管理员解除患者模式” action while patient mode is active
- Clicking it opens an admin verification dialog
- The dialog asks for administrator username and password
- Existing `/api/auth/login` is used to validate credentials
- Only role `admin` can unlock patient mode
- Successful verification disables patient mode and clears the temporary auth side effects from the unlock flow

To keep behavior predictable, unlocking patient mode does not auto-enter the admin console. It only restores normal login mode.

## Frontend Architecture

### Patient mode state service

Add a new small service module in `frontend/src/services` to encapsulate patient-mode state.

Responsibilities:
- Read mode from `localStorage`
- Enable mode
- Disable mode
- Check whether a user/route is allowed while the mode is active

This keeps mode logic out of the login page and router details.

### Router guard integration

The router guard should enforce patient mode globally for the current browser session.

Rules:
- If patient mode is enabled and the route role is `doctor` or `admin`, redirect to `/login`
- If the current session belongs to `doctor` or `admin` while patient mode is enabled, clear that auth session before redirecting
- Patient routes remain reachable

### Login page modes

The login page should render two distinct states:

Normal mode:
- Formal multi-role access presentation
- Three access cards remain visible, but without demo wording
- Patient self-registration remains available
- A visible control allows entering patient mode

Patient mode:
- Only patient login and registration remain visible
- The page prominently indicates that the browser is currently in patient service mode
- A single unlock action is available for administrators

## UI Direction

The existing warm medical operations-hub visual direction remains the base. The new changes should preserve that polish while shifting the product voice from “demo” to “real system”.

UI principles:
- Formal, service-oriented wording
- No “demo” badges or helper labels on the main entry
- Strong visual distinction for patient mode status without making the screen feel like an error state
- Unlock action should feel controlled and administrative, not like a hidden developer backdoor

## Error Handling

### Unlock failure
- If credentials are wrong, show a concise error message
- If the authenticated user is not an admin, reject unlock and surface a role-specific message
- Do not persist the temporary unlock session as the active app session

### Route blocking
- If a doctor/admin route is hit during patient mode, redirect to `/login`
- Optionally show a one-line warning that the browser is currently in patient mode

## Testing Strategy

Follow TDD.

Add or extend frontend script tests to cover:
- Login/shell copy no longer includes explicit demo wording in the touched surfaces
- Patient mode toggles login-page presentation correctly
- Patient mode blocks doctor/admin route access
- Only real admin credentials can disable patient mode

Final verification:
- `npm run test:ci`
- `npm run build`
- `npm run verify:bundle`
