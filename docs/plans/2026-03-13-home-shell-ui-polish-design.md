# Home And Shell UI Polish Design

## Context

The frontend is now functionally stable enough for manual end-to-end testing, but the first-impression surfaces still feel like separate functional pages rather than one coherent hospital product. The most visible opportunity is concentrated in the login entry, the shared application shell, and the three role dashboards.

## Goal

Create a unified "warm medical operations hub" visual language across:
- `frontend/src/layout/MainLayout.vue`
- `frontend/src/views/common/LoginPage.vue`
- `frontend/src/views/patient/PatientDashboard.vue`
- `frontend/src/views/doctor/DoctorDashboard.vue`
- `frontend/src/views/admin/AdminDashboard.vue`

The result should feel more intentional and presentation-ready without changing business flows.

## Visual Direction

### Core style

Use a warm healthcare control-center aesthetic:
- teal and blue-green as the core medical tone
- amber highlights for warmth and operational emphasis
- soft off-white backgrounds instead of flat white
- light glass/card layering rather than dense enterprise panels

### Typography and hierarchy

Keep the existing stack, but increase contrast through spacing, size, and weight:
- larger hero titles
- tighter eyebrow labels
- cleaner section subtitles
- more deliberate card hierarchy

### Motion

Use lightweight motion only:
- fade-in and slight lift on hero/stat cards
- hover emphasis with transform and shadow
- no heavy animation library

## Component Design

### Shared shell

`MainLayout.vue` becomes the primary brand frame:
- richer top banner with gradient depth and warm highlight glow
- lighter floating navigation panel instead of a hard sidebar block
- stronger visual separation between frame and content canvas
- consistent content container spacing for all roles

### Login page

`LoginPage.vue` becomes a welcome hall:
- left hero panel feels like an entry briefing card
- right login panel gets a cleaner premium card treatment
- preset cards become more intentional and tactile
- registration path remains clear but visually secondary to login

### Patient dashboard

Patient home should feel reassuring and guided:
- softer gradients and rounded cards
- onboarding/welcome panel gets more warmth and a clearer primary action
- timeline and quick actions feel more curated than utility-only

### Doctor dashboard

Doctor home should feel efficient and clinical:
- cooler blue-teal emphasis inside the shared system language
- hero area behaves like a workbench header
- stat cards and action cluster feel compact and purposeful

### Admin dashboard

Admin home should feel like an operations hub:
- stronger amber/teal contrast
- KPI cards feel more executive and directional
- overview and alerts gain clearer visual weight

## Responsive Behavior

- Desktop is the primary design target.
- Mobile keeps the same hierarchy but collapses hero/action areas vertically.
- No horizontal scrolling should be introduced.

## Testing Strategy

- Add one lightweight frontend script assertion for the new shell/dashboard theme markers.
- Reuse the existing script suite for behavioral confidence.
- Verify with `npm run test:ci`, `npm run build`, and `npm run verify:bundle`.

## Non-Goals

- No route rewiring
- No backend changes
- No full-site restyle of secondary pages in this pass
- No screenshot testing or animation library adoption