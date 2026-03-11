# Patient Dashboard Appointment Integration Design

## Context

The patient dashboard is still driven by static demo content, while the appointment and payment pages already read real appointment data from the Spring Boot backend. We want the dashboard to become a real patient overview without adding new backend APIs or expanding scope into reports, invoices, or other domains.

## Goals

- Show real dashboard summary cards for the active demo patient.
- Show a recent appointment timeline derived from real appointment data.
- Make dashboard quick actions navigate to real patient pages.
- Keep the implementation lightweight and demo-friendly.

## Non-Goals

- No new backend endpoints.
- No changes to appointment or payment state machines.
- No real report, invoice, or notification data.
- No redesign of the overall patient layout.

## Data Source

The dashboard will reuse `fetchMyAppointments(patientId)` as its only data source. The active patient continues to come from `patientSession` local storage.

## UI Design

### Summary cards

The dashboard will render four cards from real appointment data:

1. `今日预约`: count of appointments whose `date` is today and whose business status is `BOOKED`.
2. `待支付账单`: count of appointments whose status is `BOOKED` and payment status is `UNPAID`.
3. `本月预约数`: count of appointments created in the current month.
4. `已支付预约`: count of appointments whose payment status is `PAID`.

Each card keeps a short descriptive subtitle derived from the same live data. When the count is zero, the subtitle should still read naturally.

### Recent timeline

The dashboard will display up to five recent appointment events sorted by `createdAt` descending. Timeline copy is mapped from appointment business state and payment state:

- `BOOKED + UNPAID` => `已预约，待支付`
- `BOOKED + PAID` => `预约已支付`
- `CANCELLED + UNPAID` => `预约已取消`
- `CANCELLED + REFUNDED` => `预约已取消，已退款`
- `RESCHEDULED + UNPAID` => `原预约已改约`

If no appointments exist, show a single empty-state timeline item that nudges the patient toward creating the first appointment.

### Quick actions

The dashboard quick actions become real navigation shortcuts:

- `新建预约挂号` => `/patient/appointments`
- `查看我的预约` => `/patient/appointments`
- `处理待支付账单` => `/patient/payments`
- `查看检查报告` => `/patient/reports`

Action subtitles should react to real data where relevant, for example `当前有 X 笔待支付` or `暂无待支付账单`.

## Implementation Approach

Create a small frontend mapping module that accepts appointment records and a `today` string, then returns dashboard cards, timeline rows, and quick action metadata. Keeping the mapping logic separate makes it easy to verify with a small Node-based regression script without introducing a full frontend test runner.

The Vue page will only be responsible for loading the active patient, fetching appointments, calling the mapping helpers, rendering loading and empty states, and performing navigation via `vue-router`.

## Error Handling

If appointment loading fails, show an Element Plus error message and keep the dashboard visible with empty-state summaries rather than crashing the page.

## Verification

- Run a small Node regression script for dashboard mapping logic.
- Run `npm run build`.
- Run `npm run verify:bundle`.
- Manually smoke the dashboard in the browser-equivalent runtime by checking summary cards, recent timeline, and quick-action navigation states.