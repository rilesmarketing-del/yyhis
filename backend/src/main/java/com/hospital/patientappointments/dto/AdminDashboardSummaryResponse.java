package com.hospital.patientappointments.dto;

import java.util.List;

public class AdminDashboardSummaryResponse {

    private Stats stats;
    private Overview overview;
    private List<AlertItem> alerts;

    public AdminDashboardSummaryResponse() {
    }

    public AdminDashboardSummaryResponse(Stats stats, Overview overview, List<AlertItem> alerts) {
        this.stats = stats;
        this.overview = overview;
        this.alerts = alerts;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }

    public List<AlertItem> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertItem> alerts) {
        this.alerts = alerts;
    }

    public static class Stats {
        private int activeSchedules;
        private int todayAppointments;
        private int todayVisits;
        private int totalPatients;

        public Stats() {
        }

        public Stats(int activeSchedules, int todayAppointments, int todayVisits, int totalPatients) {
            this.activeSchedules = activeSchedules;
            this.todayAppointments = todayAppointments;
            this.todayVisits = todayVisits;
            this.totalPatients = totalPatients;
        }

        public int getActiveSchedules() {
            return activeSchedules;
        }

        public void setActiveSchedules(int activeSchedules) {
            this.activeSchedules = activeSchedules;
        }

        public int getTodayAppointments() {
            return todayAppointments;
        }

        public void setTodayAppointments(int todayAppointments) {
            this.todayAppointments = todayAppointments;
        }

        public int getTodayVisits() {
            return todayVisits;
        }

        public void setTodayVisits(int todayVisits) {
            this.todayVisits = todayVisits;
        }

        public int getTotalPatients() {
            return totalPatients;
        }

        public void setTotalPatients(int totalPatients) {
            this.totalPatients = totalPatients;
        }
    }

    public static class Overview {
        private int totalSchedules;
        private int todayActiveSchedules;
        private int todayAppointments;
        private int todayCompletedVisits;

        public Overview() {
        }

        public Overview(int totalSchedules, int todayActiveSchedules, int todayAppointments, int todayCompletedVisits) {
            this.totalSchedules = totalSchedules;
            this.todayActiveSchedules = todayActiveSchedules;
            this.todayAppointments = todayAppointments;
            this.todayCompletedVisits = todayCompletedVisits;
        }

        public int getTotalSchedules() {
            return totalSchedules;
        }

        public void setTotalSchedules(int totalSchedules) {
            this.totalSchedules = totalSchedules;
        }

        public int getTodayActiveSchedules() {
            return todayActiveSchedules;
        }

        public void setTodayActiveSchedules(int todayActiveSchedules) {
            this.todayActiveSchedules = todayActiveSchedules;
        }

        public int getTodayAppointments() {
            return todayAppointments;
        }

        public void setTodayAppointments(int todayAppointments) {
            this.todayAppointments = todayAppointments;
        }

        public int getTodayCompletedVisits() {
            return todayCompletedVisits;
        }

        public void setTodayCompletedVisits(int todayCompletedVisits) {
            this.todayCompletedVisits = todayCompletedVisits;
        }
    }

    public static class AlertItem {
        private String level;
        private String message;

        public AlertItem() {
        }

        public AlertItem(String level, String message) {
            this.level = level;
            this.message = message;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}