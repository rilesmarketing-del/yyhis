package com.hospital.patientappointments.dto;

import java.util.List;

public class AdminBillingOverviewResponse {

    private Cards cards;
    private List<AppointmentRecordResponse> bills;

    public AdminBillingOverviewResponse() {
    }

    public AdminBillingOverviewResponse(Cards cards, List<AppointmentRecordResponse> bills) {
        this.cards = cards;
        this.bills = bills;
    }

    public Cards getCards() {
        return cards;
    }

    public void setCards(Cards cards) {
        this.cards = cards;
    }

    public List<AppointmentRecordResponse> getBills() {
        return bills;
    }

    public void setBills(List<AppointmentRecordResponse> bills) {
        this.bills = bills;
    }

    public static class Cards {
        private int unpaidCount;
        private int paidCount;
        private int refundedCount;

        public Cards() {
        }

        public Cards(int unpaidCount, int paidCount, int refundedCount) {
            this.unpaidCount = unpaidCount;
            this.paidCount = paidCount;
            this.refundedCount = refundedCount;
        }

        public int getUnpaidCount() {
            return unpaidCount;
        }

        public void setUnpaidCount(int unpaidCount) {
            this.unpaidCount = unpaidCount;
        }

        public int getPaidCount() {
            return paidCount;
        }

        public void setPaidCount(int paidCount) {
            this.paidCount = paidCount;
        }

        public int getRefundedCount() {
            return refundedCount;
        }

        public void setRefundedCount(int refundedCount) {
            this.refundedCount = refundedCount;
        }
    }
}