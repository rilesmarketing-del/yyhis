import { createRouter, createWebHistory } from "vue-router";
import MainLayout from "../layout/MainLayout.vue";
import { roleMenus, roleMeta } from "../config/menu";
import { clearAuthSession, ensureCurrentUser } from "../services/auth";
import { isPatientModeEnabled, isRoleBlockedInPatientMode } from "../services/patientMode";

const loadPagePlaceholder = () => import("../views/common/PagePlaceholder.vue");
const loadNotFound = () => import("../views/common/NotFound.vue");
const loadLoginPage = () => import("../views/common/LoginPage.vue");

const roleComponentMap = {
  patient: {
    dashboard: () => import("../views/patient/PatientDashboard.vue"),
    appointments: () => import("../views/patient/PatientAppointments.vue"),
    visits: () => import("../views/patient/PatientVisits.vue"),
    reports: () => import("../views/patient/PatientReports.vue"),
    prescriptions: () => import("../views/patient/PatientPrescriptions.vue"),
    payments: () => import("../views/patient/PatientPayments.vue"),
  },
  doctor: {
    dashboard: () => import("../views/doctor/DoctorDashboard.vue"),
    clinic: () => import("../views/doctor/DoctorClinic.vue"),
    records: () => import("../views/doctor/DoctorRecords.vue"),
    orders: () => import("../views/doctor/DoctorOrders.vue"),
    patients: () => import("../views/doctor/DoctorPatients.vue"),
    schedule: () => import("../views/doctor/DoctorSchedule.vue"),
  },
  admin: {
    dashboard: () => import("../views/admin/AdminDashboard.vue"),
    org: () => import("../views/admin/AdminOrg.vue"),
    auth: () => import("../views/admin/AdminAuth.vue"),
    scheduling: () => import("../views/admin/AdminScheduling.vue"),
    pharmacy: () => import("../views/admin/AdminPharmacy.vue"),
    billing: () => import("../views/admin/AdminBilling.vue"),
    reports: () => import("../views/admin/AdminReports.vue"),
    system: () => import("../views/admin/AdminSystem.vue"),
  },
};

function buildRoleChildren(role) {
  return roleMenus[role].map((item) => ({
    path: item.path.replace(`/${role}/`, ""),
    name: `${role}-${item.key}`,
    component: roleComponentMap[role]?.[item.key] || loadPagePlaceholder,
    meta: {
      role,
      title: item.title,
      modulePath: item.path,
      children: item.children,
    },
  }));
}

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "login",
    component: loadLoginPage,
    meta: { public: true, title: "登录" },
  },
  {
    path: "/patient",
    component: MainLayout,
    meta: { role: "patient" },
    children: [...buildRoleChildren("patient"), { path: "", redirect: "/patient/dashboard" }],
  },
  {
    path: "/doctor",
    component: MainLayout,
    meta: { role: "doctor" },
    children: [...buildRoleChildren("doctor"), { path: "", redirect: "/doctor/dashboard" }],
  },
  {
    path: "/admin",
    component: MainLayout,
    meta: { role: "admin" },
    children: [...buildRoleChildren("admin"), { path: "", redirect: "/admin/dashboard" }],
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: loadNotFound,
    meta: { public: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  if (to.meta?.public) {
    if (to.path === "/login") {
      if (to.query.launcher === "fresh") {
        clearAuthSession();
        return true;
      }

      const user = await ensureCurrentUser();
      if (user?.role && !isRoleBlockedInPatientMode(user.role)) {
        return roleMeta[user.role]?.homePath || "/";
      }
      if (user?.role && isPatientModeEnabled()) {
        clearAuthSession();
      }
    }
    return true;
  }

  const requestedRole = to.meta?.role;
  if (isRoleBlockedInPatientMode(requestedRole)) {
    clearAuthSession();
    return {
      path: "/login",
      query: { patientMode: "enabled" },
    };
  }

  const user = await ensureCurrentUser();
  if (!user?.role) {
    return {
      path: "/login",
      query: { redirect: to.fullPath },
    };
  }

  if (isRoleBlockedInPatientMode(user.role)) {
    clearAuthSession();
    return {
      path: "/login",
      query: { patientMode: "enabled" },
    };
  }

  if (requestedRole && requestedRole !== user.role) {
    return roleMeta[user.role]?.homePath || "/login";
  }

  return true;
});

export default router;
