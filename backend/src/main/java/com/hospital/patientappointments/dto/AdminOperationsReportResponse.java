package com.hospital.patientappointments.dto;

import java.util.List;

public class AdminOperationsReportResponse {

    private Cards cards;
    private List<TableRow> table;

    public AdminOperationsReportResponse() {
    }

    public AdminOperationsReportResponse(Cards cards, List<TableRow> table) {
        this.cards = cards;
        this.table = table;
    }

    public Cards getCards() {
        return cards;
    }

    public void setCards(Cards cards) {
        this.cards = cards;
    }

    public List<TableRow> getTable() {
        return table;
    }

    public void setTable(List<TableRow> table) {
        this.table = table;
    }

    public static class Cards {
        private int activeSchedules;
        private int todayAppointments;
        private int todayVisits;
        private int totalPatients;

        public Cards() {
        }

        public Cards(int activeSchedules, int todayAppointments, int todayVisits, int totalPatients) {
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

    public static class TableRow {
        private String metric;
        private String value;

        public TableRow() {
        }

        public TableRow(String metric, String value) {
            this.metric = metric;
            this.value = value;
        }

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}