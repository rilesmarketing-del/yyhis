package com.hospital.patientappointments.dto;

import java.util.List;

public class AdminPharmacyOverviewResponse {

    private Cards cards;
    private List<VisitRecordResponse> records;

    public AdminPharmacyOverviewResponse() {
    }

    public AdminPharmacyOverviewResponse(Cards cards, List<VisitRecordResponse> records) {
        this.cards = cards;
        this.records = records;
    }

    public Cards getCards() {
        return cards;
    }

    public void setCards(Cards cards) {
        this.cards = cards;
    }

    public List<VisitRecordResponse> getRecords() {
        return records;
    }

    public void setRecords(List<VisitRecordResponse> records) {
        this.records = records;
    }

    public static class Cards {
        private int totalPrescriptions;
        private int todayPrescriptions;
        private int patientCount;

        public Cards() {
        }

        public Cards(int totalPrescriptions, int todayPrescriptions, int patientCount) {
            this.totalPrescriptions = totalPrescriptions;
            this.todayPrescriptions = todayPrescriptions;
            this.patientCount = patientCount;
        }

        public int getTotalPrescriptions() {
            return totalPrescriptions;
        }

        public void setTotalPrescriptions(int totalPrescriptions) {
            this.totalPrescriptions = totalPrescriptions;
        }

        public int getTodayPrescriptions() {
            return todayPrescriptions;
        }

        public void setTodayPrescriptions(int todayPrescriptions) {
            this.todayPrescriptions = todayPrescriptions;
        }

        public int getPatientCount() {
            return patientCount;
        }

        public void setPatientCount(int patientCount) {
            this.patientCount = patientCount;
        }
    }
}