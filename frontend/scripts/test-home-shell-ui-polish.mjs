import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

async function expectMarker(relativePath, marker) {
  const source = await readFile(new URL(relativePath, import.meta.url), "utf8");
  assert.match(source, marker, `${relativePath} is missing ${marker}`);
}

await expectMarker("../src/layout/MainLayout.vue", /medical-hub-shell/);
await expectMarker("../src/views/common/LoginPage.vue", /welcome-orbit/);
await expectMarker("../src/views/patient/PatientDashboard.vue", /patient-warmth-panel/);
await expectMarker("../src/views/doctor/DoctorDashboard.vue", /doctor-command-panel/);
await expectMarker("../src/views/admin/AdminDashboard.vue", /admin-ops-panel/);

console.log("home shell ui polish markers passed");
