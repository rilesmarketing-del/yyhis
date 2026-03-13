# Frontend Copy And CI Sanitization Design

## Context

A shell-based inspection of the frontend suggested mojibake in shared copy sources such as `frontend/src/config/menu.js`, `frontend/src/layout/MainLayout.vue`, and `frontend/src/views/common/LoginPage.vue`. A source-level verification pass was required before touching production files.

That verification showed the frontend source itself is healthy: Node-based reads and module imports returned the expected Chinese copy. The mojibake signal came from terminal rendering, not from the actual files. The real local hardening opportunity is therefore to preserve this known-good frontend state with stronger script coverage and to refresh one stale admin-org regression script.

## Goals

- Verify that shared frontend copy sources are readable and stable.
- Refresh the outdated admin organization mapping script so it matches current behavior.
- Bring the maintained frontend verification scripts under `npm run test:ci`.
- Avoid unnecessary edits to production Vue files when the source is already correct.

## Non-Goals

- Rewriting production copy that is already correct.
- Redesigning layout or UI styling.
- Changing backend contracts.
- Introducing browser automation in this pass.

## Approach

### 1. Add a shared copy guard

Create a focused frontend script that verifies:
- `roleMeta` and `roleMenus` export readable Chinese labels.
- `MainLayout.vue` contains the expected shell copy.
- `LoginPage.vue` contains the expected onboarding and login/register copy.
- `auth.js` still exposes the intended fallback error message.

This captures the real source-of-truth state and protects against future regressions without adding a Vue mounting stack.

### 2. Refresh outdated admin-org script expectations

Update `frontend/scripts/test-admin-org.mjs` so it matches the current admin organization formatting and label conventions. Keep `frontend/scripts/test-admin-org-governance.mjs` aligned with the same readable Chinese expectations.

### 3. Widen `test:ci`

Expand `frontend/package.json` so `npm run test:ci` runs the maintained frontend script set, including the new common-copy guard and the currently ad hoc dashboard/detail scripts that already pass individually.

## Testing Strategy

- Add the new common-copy script and refresh the admin-org script expectations.
- Individually run the refreshed scripts to confirm they describe current behavior correctly.
- Run the widened `npm run test:ci`, then `npm run build` and `npm run verify:bundle`.

## Risks And Mitigations

- Risk: terminal mojibake could be mistaken for a real source regression.
  - Mitigation: verify via Node imports and UTF-8 file reads before changing production files.
- Risk: widening `test:ci` could include stale scripts.
  - Mitigation: individually execute every script before aggregating it.